package broadwick.graph.algorithms;

import broadwick.graph.Edge;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import broadwick.graph.Graph;
import broadwick.graph.Vertex;
import org.apache.commons.collections15.Transformer;

/**
 * Calculates distances in a specified graph, using Dijkstra's single-source-shortest-path algorithm. All edge weights
 * in the graph must be nonnegative; otherwise an
 * <code>IllegalArgumentException</code> will be thrown.
 */
public class ShortestPath<V extends Vertex, E extends Edge> {

    /**
     *
     * @param graph
     */
    public ShortestPath(final Graph<V, E> graph) {

        jungGraph = new edu.uci.ics.jung.graph.UndirectedSparseMultigraph();
        for (E edge : graph.getEdges()) {
            jungGraph.addEdge(edge, edge.getSource(), edge.getDestination());
        }

        weightTransformer = new EdgeWeightTransformer();
    }

    /**
     * Returns the length of a shortest path from the source to the target vertex, or null if the target is not
     * reachable from the source. If either vertex is not in the graph for which this instance was created, throws
     * <code>IllegalArgumentException</code>.
     * @param source the source
     * @param target the target
     * @return the distance from source to target
     */
    public final long calculateDistance(final V source, final V target) {

        final DijkstraDistance<V, E> distance = new DijkstraDistance<>(jungGraph, weightTransformer);
        final Number d = distance.getDistance(source, target);
        return d == null ? 0 : d.longValue();
    }
    private edu.uci.ics.jung.graph.UndirectedSparseMultigraph jungGraph;
    private Transformer<E, Number> weightTransformer;
    
    private class EdgeWeightTransformer implements Transformer<E, Number> {

        @Override
        public Number transform(E edge) {
            if (edge instanceof Edge) {
                return ((Edge) edge).getWeight();
            } else {
                return 0.0;
            }
        }
    }
}
