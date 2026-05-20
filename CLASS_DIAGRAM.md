# DesignDraw — Diagramme de classes complet

> Vue exhaustive de toutes les classes du projet, regroupées par package,
> avec les attributs principaux, les méthodes publiques et les relations
> inter-classes (héritage, implémentation, composition, dépendance).
>
> Pour la justification de chaque patron, voir **`DOCUMENTATION.md`**.

---

## 1. Légende

| Notation Mermaid | Signification |
|---|---|
| `A <|.. B` | `B` implémente l'interface `A` |
| `A <|-- B` | `B` hérite de `A` (extends) |
| `A o--> B` | `A` possède `B` (composition / agrégation) |
| `A ..> B`  | `A` dépend de `B` (utilise / crée) |
| `<<interface>>` | type interface |
| `<<abstract>>`  | classe abstraite |
| `+` `-` `#` | visibilité : public / private / protected |
| `$` (suffixe) | membre `static` |

---

## 2. Diagramme global (vue d'ensemble)

```mermaid
classDiagram
    direction LR

    %% --- Entrée ---
    class HelloFX {
        +start(Stage)
        +main(String[])$
    }

    %% --- Patrons (résumé) ---
    class IShape { <<interface>> }
    class ShapeFactory { <<interface>> }
    class ShapeDecorator { <<abstract>> }
    class ICommand { <<interface>> }
    class CommandManager
    class ILogger { <<interface>> }
    class Logger
    class IShortestPathStrategy { <<interface>> }
    class PathCalculator
    class IObservable { <<interface>> }
    class IObserver { <<interface>> }
    class DrawingCanvas
    class ToolPalette
    class DrawingRepository
    class DatabaseConnection

    HelloFX ..> ToolPalette
    HelloFX ..> DrawingCanvas
    HelloFX ..> CommandManager
    HelloFX ..> Logger
    HelloFX ..> PathCalculator
    HelloFX ..> DrawingRepository

    %% Factory + Decorator + Observer
    ToolPalette ..> ShapeFactory : Factory Method
    ToolPalette ..> ShapeDecorator : Decorator
    ShapeFactory ..> IShape : crée
    ShapeDecorator ..|> IShape
    ShapeDecorator o--> IShape : wrapped
    IObservable <|-- IShape
    IShape ..> IObserver : notifyObservers
    DrawingCanvas ..|> IObserver
    DrawingCanvas ..> IShape : observe

    %% Command
    CommandManager o--> ICommand
    ICommand ..> DrawingCanvas : Receiver (add/erase)
    ICommand ..> IShape : Receiver (resize)

    %% Strategy + Singleton
    Logger o--> ILogger : Strategy
    PathCalculator o--> IShortestPathStrategy : Strategy

    %% Persistance
    DrawingRepository ..> DatabaseConnection : Singleton
    DrawingRepository ..> ShapeFactory : reconstruit
    DrawingRepository ..> ShapeDecorator : ré-empile couleurs
    DrawingRepository ..> IShape
```

---

## 3. Package `dp.DS.observer` — Observer + modèle des formes

```mermaid
classDiagram
    direction TB

    class IObservable {
        <<interface>>
        +addObserver(IObserver)
        +removeObserver(IObserver)
        +notifyObservers()
    }

    class IObserver {
        <<interface>>
        +update()
    }

    class IShape {
        <<interface>>
        +draw(GraphicsContext) default
        +fillShape(GraphicsContext) default
        +strokeShape(GraphicsContext) default
        +contains(double, double) boolean default
        +resize(double, double) default
        +resizeTo(double) default
        +getStartX() double default
        +getStartY() double default
        +getEndX() double default
        +getEndY() double default
        +getCenterX() double default
        +getCenterY() double default
        +shapeKind() String default
        +is3D() boolean default
    }

    class RectangleShape {
        -x, y, width, height : double
        -observers : List~IObserver~
        +RectangleShape(x, y, w, h)
        +fillShape(gc)
        +strokeShape(gc)
        +resize(ex, ey) "notifyObservers()"
        +resizeTo(size) "notifyObservers()"
        +shapeKind() "RECTANGLE"
    }
    class RectangleShape3D {
        +is3D() true
    }

    class CircleShape {
        -x, y, width, height : double
        -observers : List~IObserver~
        +fillShape(gc)
        +strokeShape(gc)
        +resize(ex, ey) "notifyObservers()"
        +resizeTo(size) "notifyObservers()"
        +shapeKind() "CIRCLE"
    }
    class CircleShape3D {
        +is3D() true
    }

    class LineShape {
        -startX, startY, endX, endY : double
        -observers : List~IObserver~
        +strokeShape(gc)
        +resize(ex, ey) "notifyObservers()"
        +resizeTo(size) "notifyObservers()"
        +shapeKind() "LINE"
    }
    class LineShape3D {
        +is3D() true
    }

    class DrawingCanvas {
        -canvas : Canvas
        -container : Pane
        -shapes : List~IShape~
        -highlightPath : List~double[]~
        +addShape(IShape)
        +removeShape(IShape)
        +replaceShape(old, new) "swap, preserve z-order"
        +clearShapes()
        +findShapeAt(x, y) IShape
        +getLastShape() IShape
        +getShapes() List~IShape~
        +setHighlightPath(List)
        +clearHighlight()
        +redraw()
        +update() "redraw()"
    }

    class ToolPalette {
        -sf : ShapeFactory
        -fillColorPicker : ColorPicker
        -borderColorPicker : ColorPicker
        -cbFill, cbBorder, cbFixedSize : CheckBox
        -comboResize, comboLogger : ComboBox
        -selectedShapeType : String
        -is3D, eraserMode, resizeMode, pathMode, recolorMode : boolean
        +ToolPalette(...callbacks)
        +createShape(sx, sy, ex, ey) IShape
        +recolor(IShape) IShape
        +getSelectedSize() int
        +isEraserMode() boolean
        +isResizeMode() boolean
        +isPathMode() boolean
        +isRecolorMode() boolean
        +setSelectedLogger(String)
        +getView() VBox
    }

    IObservable <|-- IShape
    IShape <|.. RectangleShape
    IShape <|.. CircleShape
    IShape <|.. LineShape
    RectangleShape <|.. RectangleShape3D : (variante 3D)
    CircleShape    <|.. CircleShape3D    : (variante 3D)
    LineShape      <|.. LineShape3D      : (variante 3D)

    IObserver <|.. DrawingCanvas
    DrawingCanvas o--> IShape : shapes
    DrawingCanvas ..> IShape : addObserver(this)

    ToolPalette ..> IShape : produit (via Factory + Decorator)
```

> **Note** : les `*3D` n'héritent pas réellement de leurs équivalents 2D dans
> le code — chaque variante implémente directement `IShape`. Le diagramme
> ci-dessus regroupe visuellement par parenté logique ; pour la hiérarchie
> *exacte*, voir le diagramme **§ Hiérarchie d'implémentation** plus bas.

---

## 4. Package `dp.DS.Factory` — Factory Method

```mermaid
classDiagram
    direction LR

    class ShapeFactory {
        <<interface>>
        +createShape(sx, sy, ex, ey) IShape
    }

    class RectangleFactory   { +createShape(...) IShape }
    class RectangleFactory3D { +createShape(...) IShape }
    class CircleFactory      { +createShape(...) IShape }
    class CircleFactory3D    { +createShape(...) IShape }
    class LineFactory        { +createShape(...) IShape }
    class LineFactory3D      { +createShape(...) IShape }

    class IShape { <<interface>> }

    ShapeFactory <|.. RectangleFactory
    ShapeFactory <|.. RectangleFactory3D
    ShapeFactory <|.. CircleFactory
    ShapeFactory <|.. CircleFactory3D
    ShapeFactory <|.. LineFactory
    ShapeFactory <|.. LineFactory3D
    ShapeFactory ..> IShape : crée

    RectangleFactory ..> IShape : new RectangleShape
    RectangleFactory3D ..> IShape : new RectangleShape3D
    CircleFactory ..> IShape : new CircleShape
    CircleFactory3D ..> IShape : new CircleShape3D
    LineFactory ..> IShape : new LineShape
    LineFactory3D ..> IShape : new LineShape3D
```

---

## 5. Package `dp.DS.decorator` — Decorator

```mermaid
classDiagram
    direction TB

    class IShape {
        <<interface>>
        +draw(gc)
        +fillShape(gc)
        +strokeShape(gc)
    }

    class ShapeDecorator {
        <<abstract>>
        #wrapped : IShape
        +ShapeDecorator(IShape)
        +getWrapped() IShape
        +draw(gc)
        +fillShape(gc)
        +strokeShape(gc)
        +contains(x, y) boolean
        +resize(ex, ey)
        +resizeTo(size)
        +addObserver(o)
        +removeObserver(o)
        +notifyObservers()
    }

    class FillColorDecorator {
        -fillColor : Color
        +FillColorDecorator(IShape, Color)
        +getFillColor() Color
        +draw(gc) "wrapped.draw() + fillShape"
    }

    class BorderColorDecorator {
        -borderColor : Color
        +BorderColorDecorator(IShape, Color)
        +getBorderColor() Color
        +draw(gc) "wrapped.draw() + strokeShape"
    }

    IShape <|.. ShapeDecorator
    ShapeDecorator o--> IShape : wrapped
    ShapeDecorator <|-- FillColorDecorator
    ShapeDecorator <|-- BorderColorDecorator
```

> **Contrat** : `FillColorDecorator.draw()` et `BorderColorDecorator.draw()`
> commencent **tous deux** par `wrapped.draw(gc)` puis ajoutent leur propre
> couche (fill ou stroke). La composition est valide quel que soit l'ordre ;
> seul le z-order varie. `ShapeDecorator` délègue `addObserver` /
> `removeObserver` / `notifyObservers` à `wrapped`, donc l'Observer traverse
> les décorateurs sans qu'ils aient à gérer leur propre liste d'observateurs.

---

## 6. Package `dp.DS.command` — Command + Undo/Redo

```mermaid
classDiagram
    direction TB

    class ICommand {
        <<interface>>
        +execute()
        +undo()
    }

    class CommandManager {
        -undoStack : Stack~ICommand~
        -redoStack : Stack~ICommand~
        +executeCommand(ICommand)
        +undo()
        +redo()
        +canUndo() boolean
        +canRedo() boolean
    }

    class AddShapeCommand {
        -canvas : DrawingCanvas
        -shape : IShape
        +AddShapeCommand(canvas, shape)
        +execute() "canvas.addShape"
        +undo() "canvas.removeShape"
    }

    class EraseShapeCommand {
        -canvas : DrawingCanvas
        -shape : IShape
        +EraseShapeCommand(canvas, shape)
        +execute() "canvas.removeShape"
        +undo() "canvas.addShape"
    }

    class ResizeShapeCommand {
        -shape : IShape
        -newSize : double
        -oldEndX, oldEndY : double
        +ResizeShapeCommand(shape, newSize)
        +execute() "shape.resizeTo"
        +undo() "shape.resize"
    }

    class ChangeColorCommand {
        -canvas : DrawingCanvas
        -oldShape : IShape
        -newShape : IShape
        +ChangeColorCommand(canvas, old, new)
        +execute() "canvas.replaceShape(old,new)"
        +undo() "canvas.replaceShape(new,old)"
    }

    class DrawingCanvas
    class IShape { <<interface>> }

    ICommand <|.. AddShapeCommand
    ICommand <|.. EraseShapeCommand
    ICommand <|.. ResizeShapeCommand
    ICommand <|.. ChangeColorCommand
    CommandManager o--> ICommand : piles undo/redo

    AddShapeCommand ..> DrawingCanvas : Receiver
    EraseShapeCommand ..> DrawingCanvas : Receiver
    AddShapeCommand ..> IShape
    EraseShapeCommand ..> IShape
    ResizeShapeCommand ..> IShape : Receiver
    ChangeColorCommand ..> DrawingCanvas : Receiver
    ChangeColorCommand ..> IShape : old + new
```

> `ResizeShapeCommand` ne référence pas `DrawingCanvas` : il mute la forme,
> et la chaîne Observer fait redessiner le canevas (cf. **D7** dans
> `DOCUMENTATION.md`).
>
> `ChangeColorCommand` ne mute pas l'ancienne chaîne de Decorators : il en
> substitue une nouvelle (construite par `ToolPalette.recolor`) autour de
> la même forme brute. `DrawingCanvas.replaceShape` préserve la position
> dans la liste (donc le z-order) et transfère l'observateur. Voir **D12**.

---

## 7. Package `dp.DS.strategy` + `dp.DS.singleton` — Strategy + Singleton (journalisation)

```mermaid
classDiagram
    direction LR

    class ILogger {
        <<interface>>
        +log(String)
    }

    class LogConsole { +log(msg) }
    class LogFile {
        -LOG_DIR : String$
        -LOG_FILE : String$
        +LogFile()
        +log(msg)
    }
    class LogDB { +log(msg) }

    class Logger {
        -strategy : ILogger
        -Logger()
        +getInstance() Logger$
        +setStrategy(ILogger)
        +getStrategy() ILogger
        +log(String)
    }
    class Logger_Holder {
        <<static nested>>
        +INSTANCE : Logger$
    }

    ILogger <|.. LogConsole
    ILogger <|.. LogFile
    ILogger <|.. LogDB

    Logger o--> ILogger : strategy
    Logger ..> Logger_Holder : INSTANCE
    Logger ..> LogConsole : défaut

    LogDB ..> DatabaseConnection : Singleton

    class DatabaseConnection
```

---

## 8. Package `dp.DS.graph` — Strategy (plus court chemin)

```mermaid
classDiagram
    direction TB

    class IShortestPathStrategy {
        <<interface>>
        +findPath(Graph, src, tgt) PathResult
    }

    class PathCalculator {
        -strategy : IShortestPathStrategy
        +PathCalculator()
        +setStrategy(IShortestPathStrategy)
        +getStrategy() IShortestPathStrategy
        +findPath(Graph, src, tgt) PathResult
    }

    class DijkstraStrategy
    class BellmanFordStrategy
    class BFSStrategy

    class Graph {
        +addNode(x, y) int
        +addEdge(u, v, w)
        +nodeCount() int
        +nodePosition(i) double[]
        +nearestNode(x, y, maxDist) int
        +neighbors(i) List
    }

    class GraphBuilder {
        +build(List~IShape~) Graph
    }

    class PathResult {
        -nodes : List~Integer~
        -distance : double
        -found : boolean
        +isFound() boolean
        +getNodes() List
        +getDistance() double
    }

    IShortestPathStrategy <|.. DijkstraStrategy
    IShortestPathStrategy <|.. BellmanFordStrategy
    IShortestPathStrategy <|.. BFSStrategy

    PathCalculator o--> IShortestPathStrategy : strategy
    PathCalculator ..> DijkstraStrategy : défaut
    PathCalculator ..> PathResult
    GraphBuilder ..> Graph : construit
    GraphBuilder ..> IShape
    IShortestPathStrategy ..> Graph
    IShortestPathStrategy ..> PathResult

    class IShape { <<interface>> }
```

---

## 9. Package `dp.config` + `dp.DS.persistence` — Singleton BD + DAO

```mermaid
classDiagram
    direction TB

    class DatabaseConnection {
        -connection : Connection
        -HOST : String$
        -DB_NAME : String$
        -USER, PASSWORD : String$
        -DatabaseConnection()
        +getInstance() DatabaseConnection$
        +getConnection() Connection
        -openConnection() Connection
        -initTable()
    }
    class DatabaseConnection_Holder {
        <<static nested>>
        +INSTANCE : DatabaseConnection$
    }

    class DrawingRepository {
        +initTable()
        +listDrawings() List~String~
        +save(name, shapes) int
        +load(name) List~IShape~
        -findOrCreateDrawing(c, name) int
        -factoryFor(kind, is3d) ShapeFactory
    }

    class ShapeFactory { <<interface>> }
    class ShapeDecorator { <<abstract>> }
    class IShape { <<interface>> }

    DatabaseConnection ..> DatabaseConnection_Holder : INSTANCE
    DrawingRepository ..> DatabaseConnection : Singleton
    DrawingRepository ..> ShapeFactory : factoryFor(kind,is3d)
    DrawingRepository ..> ShapeDecorator : ré-empile Fill/Border
    DrawingRepository ..> IShape
```

---

## 10. Hiérarchie d'implémentation exacte des formes

Schéma fidèle au code (chaque variante 3D implémente `IShape` directement, **pas** sa cousine 2D) :

```mermaid
classDiagram
    direction TB

    class IObservable { <<interface>> }
    class IShape { <<interface>> }
    class ShapeDecorator { <<abstract>> }
    class FillColorDecorator
    class BorderColorDecorator

    class RectangleShape
    class RectangleShape3D
    class CircleShape
    class CircleShape3D
    class LineShape
    class LineShape3D

    IObservable <|-- IShape
    IShape <|.. RectangleShape
    IShape <|.. RectangleShape3D
    IShape <|.. CircleShape
    IShape <|.. CircleShape3D
    IShape <|.. LineShape
    IShape <|.. LineShape3D
    IShape <|.. ShapeDecorator
    ShapeDecorator <|-- FillColorDecorator
    ShapeDecorator <|-- BorderColorDecorator
    ShapeDecorator o--> IShape : wrapped
```

---

## 11. Carte des dépendances inter-packages

```mermaid
graph LR
    main[dp.main]
    config[dp.config]
    observer[dp.DS.observer]
    factory[dp.DS.Factory]
    decorator[dp.DS.decorator]
    command[dp.DS.command]
    strategy[dp.DS.strategy]
    singleton[dp.DS.singleton]
    graph_pkg[dp.DS.graph]
    persistence[dp.DS.persistence]

    main --> observer
    main --> factory
    main --> decorator
    main --> command
    main --> singleton
    main --> graph_pkg
    main --> persistence

    factory --> observer
    decorator --> observer
    command --> observer
    singleton --> strategy
    strategy --> config
    persistence --> observer
    persistence --> factory
    persistence --> decorator
    persistence --> config
    graph_pkg --> observer
```

> Toutes les flèches pointent **du dépendant vers la dépendance**. Aucun
> cycle entre packages : la couche `observer` (modèle + vue) est centrale
> et ne dépend d'aucun autre package du projet ; `main` (composition root)
> est le seul à dépendre de tout le monde.

---

## 12. Carte « rôle GoF → classe du projet »

| Patron | Rôle GoF | Classe(s) du projet |
|---|---|---|
| Factory Method | Creator (interface) | `ShapeFactory` |
| Factory Method | ConcreteCreator | `RectangleFactory`, `CircleFactory`, `LineFactory` + variantes `*3D` |
| Factory Method | Product | `IShape` |
| Observer | Subject | `IObservable`, implémenté par toutes les formes (`RectangleShape`, …) |
| Observer | Observer | `IObserver`, implémenté par `DrawingCanvas` |
| Command | Command | `ICommand` |
| Command | ConcreteCommand | `AddShapeCommand`, `EraseShapeCommand`, `ResizeShapeCommand`, `ChangeColorCommand` |
| Command | Invoker | `CommandManager` |
| Command | Receiver | `DrawingCanvas` (add/erase/replace) ; `IShape` (resize) |
| Decorator | Component | `IShape` |
| Decorator | ConcreteComponent | `RectangleShape`, `CircleShape`, `LineShape` + `*3D` |
| Decorator | Decorator | `ShapeDecorator` (abstrait) |
| Decorator | ConcreteDecorator | `FillColorDecorator`, `BorderColorDecorator` |
| Strategy (logs) | Strategy | `ILogger` |
| Strategy (logs) | ConcreteStrategy | `LogConsole`, `LogFile`, `LogDB` |
| Strategy (logs) | Context | `Logger` |
| Strategy (path) | Strategy | `IShortestPathStrategy` |
| Strategy (path) | ConcreteStrategy | `DijkstraStrategy`, `BellmanFordStrategy`, `BFSStrategy` |
| Strategy (path) | Context | `PathCalculator` |
| Singleton | Singleton | `Logger`, `DatabaseConnection` (via *holder idiom*) |
| DAO / Repository | DAO | `DrawingRepository` |
