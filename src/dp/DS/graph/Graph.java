package dp.DS.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Modèle de graphe non orienté pondéré construit à partir des formes du canvas :
 * chaque cercle = un nœud, chaque ligne = une arête.
 * Le poids d'une arête = distance euclidienne entre les centres des deux nœuds.
 */
public class Graph {

    /** Une arête : nœud destination + poids. */
    public static class Edge {
        public final int to;
        public final double weight;
        public Edge(int to, double weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    private final List<double[]> nodes = new ArrayList<>();        // [x, y]
    private final List<List<Edge>> adjacency = new ArrayList<>();

    public int addNode(double x, double y) {
        nodes.add(new double[]{x, y});
        adjacency.add(new ArrayList<>());
        return nodes.size() - 1;
    }

    public void addEdge(int a, int b, double weight) {
        if (a == b) return;
        adjacency.get(a).add(new Edge(b, weight));
        adjacency.get(b).add(new Edge(a, weight));
    }

    public int nodeCount() { return nodes.size(); }

    public double[] nodePosition(int i) { return nodes.get(i); }

    public List<Edge> neighbors(int i) { return adjacency.get(i); }

    /** Poids de l'arête (a,b) ou +∞ si elle n'existe pas. */
    public double weightBetween(int a, int b) {
        for (Edge e : adjacency.get(a)) {
            if (e.to == b) return e.weight;
        }
        return Double.POSITIVE_INFINITY;
    }

    /** Index du nœud le plus proche de (x,y) si distance ≤ tol, sinon -1. */
    public int nearestNode(double x, double y, double tol) {
        int best = -1;
        double bestD = tol * tol;
        for (int i = 0; i < nodes.size(); i++) {
            double dx = nodes.get(i)[0] - x;
            double dy = nodes.get(i)[1] - y;
            double d = dx * dx + dy * dy;
            if (d <= bestD) {
                bestD = d;
                best = i;
            }
        }
        return best;
    }
}
