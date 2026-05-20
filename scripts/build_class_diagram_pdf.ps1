# Renders CLASS_DIAGRAM.md to CLASS_DIAGRAM.pdf via headless Chrome.
# Requires internet access on first build (loads mermaid.js + marked.js from CDN).

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$md   = Join-Path $root 'CLASS_DIAGRAM.md'
$html = Join-Path $root 'CLASS_DIAGRAM.html'
$pdf  = Join-Path $root 'CLASS_DIAGRAM.pdf'

if (-not (Test-Path $md)) { throw "Not found: $md" }

# Read markdown and base64-encode (avoids any JS-string escaping issues with
# the backticks used by ```mermaid fences).
$mdBytes = [IO.File]::ReadAllBytes($md)
$mdB64   = [Convert]::ToBase64String($mdBytes)

$template = @'
<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="UTF-8">
<title>DesignDraw — Class Diagram</title>
<script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/marked@12/marked.min.js"></script>
<style>
  :root { color-scheme: light; }
  html, body { background: white; }
  body {
    font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
    max-width: 1100px;
    margin: 40px auto;
    padding: 0 36px 60px 36px;
    color: #1a1a1a;
    line-height: 1.55;
  }
  h1 { font-size: 26px; border-bottom: 2px solid #222; padding-bottom: 8px; margin-top: 28px; }
  h2 { font-size: 20px; margin-top: 36px; border-bottom: 1px solid #ddd; padding-bottom: 4px; }
  h3 { font-size: 16px; margin-top: 24px; }
  p, li { font-size: 13px; }
  code {
    background: #f3f3f5; padding: 1px 5px; border-radius: 3px;
    font-family: Consolas, 'Cascadia Mono', Monaco, monospace; font-size: 12px;
  }
  pre {
    background: #f7f7f9; border: 1px solid #e2e2e7;
    padding: 10px 12px; border-radius: 6px;
    overflow-x: auto; font-size: 12px;
  }
  pre code { background: none; padding: 0; font-size: 12px; }
  table { border-collapse: collapse; margin: 14px 0; font-size: 12px; }
  th, td { border: 1px solid #ccc; padding: 5px 10px; text-align: left; vertical-align: top; }
  th { background: #eef0f3; }
  blockquote {
    border-left: 4px solid #c8cdd3; padding: 6px 14px; color: #444;
    margin: 14px 0; background: #f6f7f9; font-size: 12px;
  }
  hr { border: none; border-top: 1px solid #ddd; margin: 28px 0; }
  .mermaid {
    background: white; padding: 12px 0; margin: 16px 0;
    page-break-inside: avoid; text-align: center;
  }
  .mermaid svg { max-width: 100%; height: auto; }
  @page { size: A4; margin: 14mm; }
  @media print {
    body { margin: 0; padding: 0 8mm; max-width: 100%; }
    h2, h3 { page-break-after: avoid; }
    table, pre, blockquote { page-break-inside: avoid; }
    .mermaid { page-break-inside: avoid; }
  }
</style>
</head>
<body>
<div id="content">Loading…</div>
<script>
  const MD_BASE64 = "__MD_B64__";
  const md = new TextDecoder('utf-8').decode(
    Uint8Array.from(atob(MD_BASE64), c => c.charCodeAt(0))
  );

  document.getElementById('content').innerHTML = marked.parse(md, { gfm: true });

  // Convert <pre><code class="language-mermaid">...</code></pre> blocks
  // produced by marked into <div class="mermaid"> blocks that mermaid.run()
  // will render.
  document.querySelectorAll('pre code.language-mermaid').forEach(block => {
    const div = document.createElement('div');
    div.className = 'mermaid';
    div.textContent = block.textContent;
    block.parentElement.replaceWith(div);
  });

  mermaid.initialize({
    startOnLoad: false,
    theme: 'default',
    securityLevel: 'loose',
    flowchart: { useMaxWidth: true },
    class:     { useMaxWidth: true }
  });

  mermaid.run().then(() => {
    document.title = 'DesignDraw — Class Diagram (ready)';
    window.mermaidReady = true;
  }).catch(err => {
    document.title = 'DesignDraw — Class Diagram (error)';
    console.error(err);
    window.mermaidReady = true; // unblock printing anyway
  });
</script>
</body>
</html>
'@

$htmlOut = $template.Replace('__MD_B64__', $mdB64)
[IO.File]::WriteAllText($html, $htmlOut, [Text.UTF8Encoding]::new($false))
Write-Output "HTML written: $html"

# Find Chrome (prefer Chrome over Edge for the --print-to-pdf flag stability)
$chromeCandidates = @(
  "${env:ProgramFiles}\Google\Chrome\Application\chrome.exe",
  "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe",
  "${env:LocalAppData}\Google\Chrome\Application\chrome.exe",
  "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe",
  "${env:ProgramFiles}\Microsoft\Edge\Application\msedge.exe"
)
$chrome = $chromeCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $chrome) { throw "Neither Chrome nor Edge found." }
Write-Output "Browser: $chrome"

# Convert file paths to file:// URIs (Chrome needs forward slashes).
$htmlUri = 'file:///' + ($html -replace '\\','/')

# --virtual-time-budget waits up to N ms of script time before snapshotting,
# which gives mermaid.run() time to finish.
$args = @(
  '--headless=new',
  '--disable-gpu',
  '--no-sandbox',
  '--disable-extensions',
  '--no-pdf-header-footer',
  '--virtual-time-budget=20000',
  "--print-to-pdf=$pdf",
  $htmlUri
)

Write-Output "Rendering to PDF…"
$proc = Start-Process -FilePath $chrome -ArgumentList $args -NoNewWindow -PassThru -Wait
if ($proc.ExitCode -ne 0) {
  throw "Chrome exited with code $($proc.ExitCode)"
}

if (-not (Test-Path $pdf)) { throw "PDF was not produced at $pdf" }
$size = (Get-Item $pdf).Length
Write-Output ("PDF written: {0} ({1:N0} bytes)" -f $pdf, $size)
