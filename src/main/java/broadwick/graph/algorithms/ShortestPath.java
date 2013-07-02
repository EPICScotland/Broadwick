package broadwick.graph.algorithms;

import broadwick.graph.Edge;
import broadwick.graph.EdgeType;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import broadwick.graph.Graph;
import broadwick.graph.Vertex;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import java.util.List;
import org.apache.commons.collections15.Transformer;

/**
 * Calculates distances in a specified graph, using Dijkstra's single-source-shortest-path algorithm. All edge weights
 * in the graph must be nonnegative; otherwise an
 * <code>IllegalArgumentException</code> will be thrown.
 * @param <V> the vertex type.
 * @param <E> the edge type.
 */
public class ShortestPath<V extends Vertex, E extends Edge> {

    /**
     * Create a ShortestPath instance.
     * @param graph the network/graph on which we will be looking for shortest paths.
     */
    public ShortestPath(final Graph<V, E> graph) {

        if (EdgeType.DIRECTED.equals(graph.getEdgeType())) {
            jungGraph = new edu.uci.ics.jung.graph.DirectedSparseMultigraph();
        } else if (EdgeType.UNDIRECTED.equals(graph.getEdgeType())) {
            jungGraph = new edu.uci.ics.jung.graph.UndirectedSparseMultigraph();
        } else {
            throw new IllegalArgumentException("Could not create ShortestPath object for unknown grpah type.");
        }
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
    public final double calculateDistance(final V source, final V target) {

        final DijkstraDistance<V, E> distance = new DijkstraDistance<>(jungGraph, weightTransformer);
        final Number d = distance.getDistance(source, target);
        return d == null ? 0 : d.doubleValue();
    }

    /**
     * Returns a List of the edges on the shortest path from source to target, in order of their occurrence on this
     * path. If either vertex is not in the graph for which this instance was created, throws IllegalArgumentException
     * @param source the vertex from which distances are measured
     * @param target the number of vertics for which to measure distances
     * @return
     */
    public final List<E> getEdgesInPath(final V source, final V target) {
        DijkstraShortestPath path = new DijkstraShortestPath(jungGraph);
        return path.getPath(source, target);
    }
    private edu.uci.ics.jung.graph.AbstractTypedGraph jungGraph;
    private Transformer<E, Number> weightTransformer;

    /**
     * Transformer class to transform the edge of a graph to a double (it's weight).
     */
    private class EdgeWeightTransformer implements Transformer<E, Number> {

        @Override
        public Number transform(final E edge) {
            // all nonnull values are instances of broadwick.graph.Edge
            if (edge != null) {
                return ((Edge) edge).getWeight();
            } else {
                return 1.0;
            }
        }
    }
}
