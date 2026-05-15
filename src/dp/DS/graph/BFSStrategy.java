package dp.DS.graph;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Stratégie concrète — parcours en largeur (BFS).
 * Trouve le chemin avec le moins d'arêtes (graphe traité comme non pondéré).
 * La distance retournée reste la longueur euclidienne du chemin trouvé.
 */
public class BFSStrategy implements IShortestPathStrategy {

    @Override
    public PathResult findPath(Graph graph, int source, int target) {
        int n = graph.nodeCount();
        if (source < 0 || target < 0 || source >= n || target >= n) {
            return PathResult.notFound();
        }

        boolean[] visited = new boolean[n];
        int[] prev = new int[n];
        Arrays.fill(prev, -1);

        Queue<Integer> queue = new ArrayDeque<>();
        visited[source] = true;
        queue.add(source);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            if (u == target) break;
            for (Graph.Edge e : graph.neighbors(u)) {
                if (!visited[e.to]) {
                    visited[e.to] = true;
                    prev[e.to] = u;
                    queue.add(e.to);
                }
            }
        }

        if (!visited[target]) {
            return PathResult.notFound();
        }

        LinkedList<Integer> path = new LinkedList<>();
        for (int at = target; at != -1; at = prev[at]) {
            path.addFirst(at);
        }

        double distance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            distance += graph.weightBetween(path.get(i), path.get(i + 1));
        }
        return new PathResult(path, distance);
    }
}
