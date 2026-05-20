// Build CLASS_DIAGRAM.html from CLASS_DIAGRAM.md.
//
// Embeds the markdown as base64 (avoids file:// fetch / CORS) and uses
// print CSS tuned so Mermaid SVG diagrams never get split across PDF pages:
// - each H2 section starts on a fresh PDF page
// - each .mermaid block is constrained to a single printable A4 page
//   (max-height: 235mm) with `break-inside: avoid` ; the SVG scales via
//   `preserveAspectRatio="xMidYMid meet"` so it fits.

const fs = require('fs');
const path = require('path');

const here = __dirname;
const MD_PATH = path.join(here, 'CLASS_DIAGRAM.md');
const HTML_PATH = path.join(here, 'CLASS_DIAGRAM.html');

const md = fs.readFileSync(MD_PATH, 'utf8');
const b64 = Buffer.from(md, 'utf8').toString('base64');

const HTML = `<!DOCTYPE html>
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
    text-align: center;
    break-inside: avoid;
    page-break-inside: avoid;
  }
  .mermaid svg { max-width: 100%; height: auto; }

  @page { size: A4; margin: 14mm; }
  @media print {
    body { margin: 0; padding: 0 8mm; max-width: 100%; }
    /* Each top-level section starts on a fresh page so the heading and
       its diagram always land together. Skip the very first one. */
    h2 { page-break-before: always; break-before: page; }
    h2:first-of-type { page-break-before: avoid; break-before: auto; }
    h1, h2, h3 { page-break-after: avoid; break-after: avoid; }
    table, pre, blockquote { page-break-inside: avoid; break-inside: avoid; }

    /* Hard cap each Mermaid diagram to a single printable A4 page.
       A4 portrait = 297 mm tall ; with 14 mm margins => 269 mm usable.
       We cap at 235 mm to leave room for the section heading + caption,
       which guarantees the diagram + heading fit on one page together. */
    .mermaid {
      break-inside: avoid;
      page-break-inside: avoid;
      max-height: 235mm;
      overflow: hidden;
    }
    .mermaid svg {
      max-height: 230mm !important;
      width: auto !important;
      height: auto !important;
      max-width: 100% !important;
    }
  }
</style>
</head>
<body>
<div id="content">Loading…</div>
<script>
  const MD_BASE64 = "__B64__";
  const md = new TextDecoder('utf-8').decode(
    Uint8Array.from(atob(MD_BASE64), c => c.charCodeAt(0))
  );

  document.getElementById('content').innerHTML = marked.parse(md, { gfm: true });

  // marked emits <pre><code class="language-mermaid">…</code></pre>. Convert
  // those to <div class="mermaid"> blocks for mermaid.run().
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
    // Drop explicit height attributes so print CSS max-height can shrink
    // each SVG cleanly via preserveAspectRatio.
    document.querySelectorAll('.mermaid svg').forEach(svg => {
      svg.removeAttribute('height');
      svg.setAttribute('preserveAspectRatio', 'xMidYMid meet');
    });
    document.title = 'DesignDraw — Class Diagram (ready)';
    window.mermaidReady = true;
  }).catch(err => {
    document.title = 'DesignDraw — Class Diagram (error)';
    console.error(err);
    window.mermaidReady = true;
  });
</script>
</body>
</html>
`;

fs.writeFileSync(HTML_PATH, HTML.replace('__B64__', b64), 'utf8');
console.log(`Wrote ${HTML_PATH} (${fs.statSync(HTML_PATH).size} bytes), md=${md.length} chars, b64=${b64.length} chars`);
