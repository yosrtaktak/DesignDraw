package dp.DS.graph;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Stratégie concrète — algorithme de Dijkstra (graphe pondéré,
 * poids positifs = distances euclidiennes entre nœuds).
 */
public class DijkstraStrategy implements IShortestPathStrategy {

    @Override
    public PathResult findPath(Graph graph, int source, int target) {
        int n = graph.nodeCount();
        if (source < 0 || target < 0 || source >= n || target >= n) {
            return PathResult.notFound();
        }

        double[] dist = new double[n];
        int[] prev = new int[n];
        boolean[] visited = new boolean[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        dist[source] = 0;

        // File de priorité ordonnée par distance courante : [nœud, distance].
        PriorityQueue<double[]> pq =
                new PriorityQueue<>((a, b) -> Double.compare(a[1], b[1]));
        pq.add(new double[]{source, 0});

        while (!pq.isEmpty()) {
            int u = (int) pq.poll()[0];
            if (visited[u]) continue;
            visited[u] = true;
            if (u == target) break;

            for (Graph.Edge e : graph.neighbors(u)) {
                if (dist[u] + e.weight < dist[e.to]) {
                    dist[e.to] = dist[u] + e.weight;
                    prev[e.to] = u;
                    pq.add(new double[]{e.to, dist[e.to]});
                }
            }
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
