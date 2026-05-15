package dp.DS.graph;

/**
 * Contexte du pattern Strategy pour le calcul du plus court chemin.
 *
 * Délègue le calcul à une stratégie IShortestPathStrategy interchangeable
 * à runtime — même principe que la classe Logger qui délègue à ILogger.
 *
 * Stratégie par défaut : Dijkstra. Changeable via setStrategy().
 */
public class PathCalculator {

    private IShortestPathStrategy strategy;

    public PathCalculator() {
        this.strategy = new DijkstraStrategy();
    }

    public void setStrategy(IShortestPathStrategy strategy) {
        this.strategy = strategy;
    }

    public IShortestPathStrategy getStrategy() {
        return this.strategy;
    }

    public PathResult findPath(Graph graph, int source, int target) {
        return strategy.findPath(graph, source, target);
    }
}
