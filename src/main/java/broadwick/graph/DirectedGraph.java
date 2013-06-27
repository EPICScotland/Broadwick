package broadwick.graph;

import broadwick.utils.Pair;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import java.util.Collection;

/**
 * A <code>DirectedGraph</code>, suitable for sparse graphs, that permits parallel edges..
 * <p/>
 * The implementation here is backed by the DelegateTree implementation in the JUNG library.
 * @param <V> the vertex type.
 * @param <E> the edge type.
 */
public class DirectedGraph<V extends Vertex, E extends Edge> implements broadwick.graph.Graph<V, E> {

    /**
     * Creates an instance of the graph.
     */
    public DirectedGraph() {
        graph = new DirectedSparseMultigraph();
    }

    @Override
    public final Collection<E> getInEdges(final V vertex) {
        return graph.getInEdges(vertex);
    }

    @Override
    public final Collection<E> getOutEdges(final V vertex) {
        return graph.getOutEdges(vertex);
    }

    @Override
    public final Collection<V> getPredecessors(final V vertex) {
        return graph.getPredecessors(vertex);
    }

    @Override
    public final Collection<V> getSuccessors(final V vertex) {
        return graph.getSuccessors(vertex);
    }

    @Override
    public final int inDegree(final V vertex) {
        return graph.inDegree(vertex);
    }

    @Override
    public final int outDegree(final V vertex) {
        return graph.outDegree(vertex);
    }

    @Override
    public final boolean isPredecessor(final V v1, final V v2) {
        return graph.isPredecessor(v1, v2);
    }

    @Override
    public final boolean isSuccessor(final V v1, final V v2) {
        return graph.isSuccessor(v1, v2);
    }

    @Override
    public final int getPredecessorCount(final V vertex) {
        return graph.getPredecessorCount(vertex);
    }

    @Override
    public final int getSuccessorCount(final V vertex) {
        return graph.getSuccessorCount(vertex);
    }

    @Override
    public final V getSource(final E directedEdge) {
        return graph.getSource(directedEdge);
    }

    @Override
    public final V getDest(final E directedEdge) {
        return graph.getDest(directedEdge);
    }

    @Override
    public final boolean isSource(final V vertex, final E edge) {
        return graph.isSource(vertex, edge);
    }

    @Override
    public final boolean isDest(final V vertex, final E edge) {
        return graph.isDest(vertex, edge);
    }

    @Override
    public final boolean addVertex(final V vertex) {
        return graph.addVertex(vertex);
    }

    @Override
    public final boolean addEdge(final E e, final V v1, final V v2) {
        return graph.addEdge(e, v1, v2);
    }

    @Override
    public final boolean addEdge(final E e, final V v1, final V v2, final EdgeType edgeType) {
        return graph.addEdge(e, v1, v2, edu.uci.ics.jung.graph.util.EdgeType.DIRECTED);
    }

    @Override
    public final Pair<V, V> getEndpoints(final E edge) {
        final edu.uci.ics.jung.graph.util.Pair<V> endpoints = graph.getEndpoints(edge);
        return new Pair<>(endpoints.getFirst(), endpoints.getSecond());
    }

    @Override
    public final V getOpposite(final V vertex, final E edge) {
        return graph.getOpposite(vertex, edge);
    }

    @Override
    public final int getVertexCount() {
        return graph.getVertexCount();
    }

    @Override
    public final Collection<V> getVertices() {
        return graph.getVertices();
    }

    @Override
    public final Collection<E> getEdges() {
        return graph.getEdges();
    }

    @Override
    public final boolean removeVertex(final V vertex) {
        return graph.removeVertex(vertex);
    }

    @Override
    public final boolean removeEdge(final E edge) {
        return graph.removeEdge(edge);
    }

    @Override
    public final EdgeType getEdgeType() {
        return EdgeType.DIRECTED;
    }
    private DirectedSparseMultigraph<V, E> graph;
}
