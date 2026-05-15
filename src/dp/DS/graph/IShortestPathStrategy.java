package dp.DS.graph;

/**
 * Strategy — algorithme de calcul du plus court chemin entre deux nœuds.
 *
 * Implémentations interchangeables à runtime, exactement comme ILogger
 * pour la journalisation (Dijkstra / Bellman-Ford / BFS).
 */
public interface IShortestPathStrategy {
    PathResult findPath(Graph graph, int source, int target);
}
