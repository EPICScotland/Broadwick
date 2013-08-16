package broadwick.graph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Iterator over all the vertices in a graph using a depth first traversal algorithm.
 * @param <V> the class type of the vertex.
 * @param <E> the class type of the edges.
 */
public class DepthFirstIterator<V extends Vertex, E extends Edge<V>> implements Iterator<V> {

    /**
     * Create the iterator over a tree. The tree is read and the vertices arranged (internally) so that they can be read
     * back one at a time in a depth-first order.
     * @param tree the tree to which to attach the iterator.
     */
    public DepthFirstIterator(final Tree<V, E> tree) {
//        this.tree = tree;
        this.vertexList = new LinkedList<>();
        traverseTree(tree);
    }

    @Override
    public final boolean hasNext() {
        return !vertexList.isEmpty();
    }

    @Override
    public final V next() {
        return (V) ((LinkedList)vertexList).pop();
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException("Removing vertices is not permitted in BreathFirstIterator.");
    }

    /**
     * Perform a depth first traversal through the tree recording, in a linked list, the vertices visited in order.
     * @param tree the tree to be traversed.
     */
    private void traverseTree(final Tree<V, E> tree) {

        final V root = tree.getRoot();
        if (root != null) {
            if (!vertexList.contains(root)) {
                vertexList.add(root);
            }
            for (V vertex : tree.getSuccessors(tree.getRoot())) {
                vertexList.add(vertex);
                final Tree<V, E> subtree = tree.getSubTree(vertex);
                traverseTree(subtree);
            }
        }
    }
//    private Tree<V, E> tree;
    private List<V> vertexList;
}
