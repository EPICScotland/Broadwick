package broadwick.graph;

import broadwick.utils.Pair;
import edu.uci.ics.jung.graph.DelegateTree;
import java.util.Collection;

/**
 * A subtype of Graph which is a (directed, rooted) tree. Actually what we have here is a rooted tree, that is, there is
 * a designated single vertex, the root, from which we measure the shortest path to each vertex, which we call its
 * depth; the maximum over all such depths is the tree's height.
 * <p/>
 * The implementation here is backed by the DelegateTree implementation in the JUNG library.
 * @param <V> the vertex type.
 * @param <E> the edge type.
 */
public class Tree<V, E> implements broadwick.graph.Graph<V, E> {

    /**
     * Creates an instance of the tree.
     */
    public Tree() {
        tree = new DelegateTree();
    }

    /**
     * Returns the root of this tree. The root is defined to be the vertex (designated either at the tree's creation
     * time, or as the first vertex to be added) with respect to which vertex depth is measured.
     * @return the root of this tree
     */
    public final V getRoot() {
        return tree.getRoot();
    }

    /**
     * Returns the maximum depth in this tree.
     * @return the maximum depth in this tree
     */
    public final int getHeight() {
        return tree.getHeight();
    }

    /**
     * Returns the (unweighted) distance of vertex from the root of this tree.
     * @param vertex the vertex whose depth is to be returned.
     * @return the length of the shortest unweighted path from vertex to the root of this tree
     */
    public final int getDepth(final V vertex) {
        return tree.getDepth(vertex);
    }

    /**
     * Will set the root of the Tree, only if the Tree is empty and the root is currently unset.
     * @param vertex the tree root to set
     * @return true if this call mutates the underlying graph
     */
    public final boolean addVertex(final V vertex) {
        return tree.addVertex(vertex);
    }

    @Override
    public final Collection<E> getInEdges(final V vertex) {
        return tree.getInEdges(vertex);
    }

    @Override
    public final Collection<E> getOutEdges(final V vertex) {
        return tree.getOutEdges(vertex);
    }

    @Override
    public final Collection<V> getPredecessors(final V vertex) {
        return tree.getPredecessors(vertex);
    }

    @Override
    public final Collection<V> getSuccessors(final V vertex) {
        return tree.getSuccessors(vertex);
    }

    @Override
    public final int inDegree(final V vertex) {
        return tree.inDegree(vertex);
    }

    @Override
    public final int outDegree(final V vertex) {
        return tree.outDegree(vertex);
    }

    @Override
    public final boolean isPredecessor(final V v1, final V v2) {
        return tree.isPredecessor(v1, v2);
    }

    @Override
    public final boolean isSuccessor(final V v1, final V v2) {
        return tree.isSuccessor(v1, v2);
    }

    @Override
    public final int getPredecessorCount(final V vertex) {
        return tree.getPredecessorCount(vertex);
    }

    @Override
    public final int getSuccessorCount(final V vertex) {
        return tree.getSuccessorCount(vertex);
    }

    @Override
    public final V getSource(final E directedEdge) {
        return tree.getSource(directedEdge);
    }

    @Override
    public final V getDest(final E directedEdge) {
        return tree.getDest(directedEdge);
    }

    @Override
    public final boolean isSource(final V vertex, final E edge) {
        return tree.isSource(vertex, edge);
    }

    @Override
    public final boolean isDest(final V vertex, final E edge) {
        return tree.isDest(vertex, edge);
    }

    @Override
    public final boolean addEdge(final E e, final V v1, final V v2) {
        return tree.addEdge(e, v1, v2);
    }

    @Override
    public final boolean addEdge(final E e, final V v1, final V v2, final EdgeType edgeType) {
        if (EdgeType.DIRECTED.equals(edgeType)) {
            return tree.addEdge(e, v1, v2, edu.uci.ics.jung.graph.util.EdgeType.DIRECTED);
        } else {
            return tree.addEdge(e, v1, v2, edu.uci.ics.jung.graph.util.EdgeType.UNDIRECTED);
        }
    }

    @Override
    public final Pair<V, V> getEndpoints(final E edge) {
        final edu.uci.ics.jung.graph.util.Pair<V> endpoints = tree.getEndpoints(edge);
        return new Pair<>(endpoints.getFirst(), endpoints.getSecond());
    }

    @Override
    public final V getOpposite(final V vertex, final E edge) {
        return tree.getOpposite(vertex, edge);
    }

    @Override
    public final int getVertexCount() {
        return tree.getVertexCount();
    }

    @Override
    public final Collection<V> getVertices() {
        return tree.getVertices();
    }
    
    @Override
    public final Collection<E> getEdges() {
        return tree.getEdges();
    }

    private DelegateTree<V, E> tree;
}
