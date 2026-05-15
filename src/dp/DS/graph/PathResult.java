package dp.DS.graph;

import java.util.Collections;
import java.util.List;

/**
 * Résultat d'un calcul de plus court chemin : la séquence ordonnée
 * des nœuds, la distance totale et un indicateur de succès.
 */
public class PathResult {

    private final List<Integer> nodes;
    private final double distance;
    private final boolean found;

    public PathResult(List<Integer> nodes, double distance) {
        this.nodes = nodes;
        this.distance = distance;
        this.found = true;
    }

    private PathResult() {
        this.nodes = Collections.emptyList();
        this.distance = Double.POSITIVE_INFINITY;
        this.found = false;
    }

    public static PathResult notFound() {
        return new PathResult();
    }

    public List<Integer> getNodes() { return nodes; }
    public double getDistance()     { return distance; }
    public boolean isFound()        { return found; }
}
