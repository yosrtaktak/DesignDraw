package dp.DS.graph;

import dp.DS.observer.IShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Construit un Graph à partir des formes déjà dessinées :
 *  - chaque forme NON-ligne (rectangle, cercle, 2D ou 3D) devient un nœud
 *    (positionné sur son centre) ;
 *  - chaque ligne devient une arête reliant les deux nœuds touchés par
 *    ses extrémités (le nœud qui contient l'extrémité, sinon le plus proche).
 *
 * Si aucune ligne ne crée d'arête (l'utilisateur n'a pas relié les formes),
 * le graphe devient COMPLET : chaque paire de nœuds est reliée. Ainsi le
 * calcul du plus court chemin renvoie toujours une distance.
 *
 * Le poids d'une arête = distance euclidienne entre les centres des nœuds.
 */
public class GraphBuilder {

    public Graph build(List<IShape> shapes) {
        Graph graph = new Graph();

        // 1) Nœuds = toutes les formes sauf les lignes.
        List<IShape> nodeShapes = new ArrayList<>();
        for (IShape s : shapes) {
            if (!"LINE".equals(s.shapeKind())) {
                graph.addNode(s.getCenterX(), s.getCenterY());
                nodeShapes.add(s);
            }
        }

        // 2) Arêtes = lignes reliant deux nœuds.
        boolean anyEdge = false;
        for (IShape s : shapes) {
            if ("LINE".equals(s.shapeKind())) {
                int a = nodeAt(nodeShapes, s.getStartX(), s.getStartY());
                int b = nodeAt(nodeShapes, s.getEndX(), s.getEndY());
                if (a != -1 && b != -1 && a != b) {
                    graph.addEdge(a, b, distance(graph, a, b));
                    anyEdge = true;
                }
            }
        }

        // 3) Repli : aucune arête dessinée -> graphe complet.
        if (!anyEdge) {
            int n = graph.nodeCount();
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    graph.addEdge(i, j, distance(graph, i, j));
                }
            }
        }
        return graph;
    }

    private double distance(Graph graph, int a, int b) {
        double[] pa = graph.nodePosition(a);
        double[] pb = graph.nodePosition(b);
        return Math.hypot(pb[0] - pa[0], pb[1] - pa[1]);
    }

    /**
     * Index du nœud touché par le point (x,y) : d'abord la forme qui
     * contient le point, sinon la forme dont le centre est le plus proche.
     */
    private int nodeAt(List<IShape> nodeShapes, double x, double y) {
        for (int i = 0; i < nodeShapes.size(); i++) {
            if (nodeShapes.get(i).contains(x, y)) return i;
        }
        int best = -1;
        double bestD = Double.POSITIVE_INFINITY;
        for (int i = 0; i < nodeShapes.size(); i++) {
            double dx = nodeShapes.get(i).getCenterX() - x;
            double dy = nodeShapes.get(i).getCenterY() - y;
            double d = dx * dx + dy * dy;
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return best;
    }
}
