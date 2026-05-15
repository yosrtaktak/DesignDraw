package dp.DS.graph;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Stratégie concrète — algorithme de Bellman-Ford (graphe pondéré).
 * Relâche toutes les arêtes (n-1) fois.
 */
public class BellmanFordStrategy implements IShortestPathStrategy {

    @Override
    public PathResult findPath(Graph graph, int source, int target) {
        int n = graph.nodeCount();
        if (source < 0 || target < 0 || source >= n || target >= n) {
            return PathResult.notFound();
        }

        double[] dist = new double[n];
        int[] prev = new int[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        dist[source] = 0;

        for (int i = 0; i < n - 1; i++) {
            boolean changed = false;
            for (int u = 0; u < n; u++) {
                if (dist[u] == Double.POSITIVE_INFINITY) continue;
                for (Graph.Edge e : graph.neighbors(u)) {
                    if (dist[u] + e.weight < dist[e.to]) {
                        dist[e.to] = dist[u] + e.weight;
                        prev[e.to] = u;
                        changed = true;
                    }
                }
            }
            if (!changed) break;
        }

        if (dist[target] == Double.POSITIVE_INFINITY) {
            return PathResult.notFound();
        }

        LinkedList<Integer> path = new LinkedList<>();
        for (int at = target; at != -1; at = prev[at]) {
            path.addFirst(at);
        }
        return new PathResult(path, dist[target]);
    }
}
