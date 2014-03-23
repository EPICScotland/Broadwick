/*
 * Copyright 2013 University of Glasgow.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package broadwick.graph;

import broadwick.utils.Pair;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.util.TreeUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A subtype of Graph which is a (directed, rooted) tree. Actually what we have here is a rooted tree, that is, there is
 * a designated single vertex, the root, from which we measure the shortest path to each vertex, which we call its
 * depth; the maximum over all such depths is the tree's height.
 * <p/>
 * The implementation here is backed by the DelegateTree implementation in the JUNG library.
 * @param <V> the vertex type.
 * @param <E> the edge type.
 */
@Slf4j
public class Tree<V extends Vertex, E extends Edge<V>> implements broadwick.graph.Graph<V, E>, Serializable  {

    /**
     * Creates an instance of the tree.
     */
    public Tree() {
        tree = new DelegateTree<>();
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

    @Override
    public final boolean addVertex(final V vertex) {
        return tree.addVertex(vertex);
    }

    /**
     * Obtain the sub-tree with
     * <code>vertex</code> as it's root.
     * @param vertex the root node of the subtree.
     * @return a tree object that is a subtree of the current tree.
     */
    public final Tree<V, E> getSubTree(final V vertex) {
        final Tree<V, E> subTree = new Tree<>();
        try {
            subTree.tree = (DelegateTree<V, E>) TreeUtils.getSubTree(tree, vertex);
        } catch (InstantiationException | IllegalAccessException ex) {
            log.error("Could not create subtree. {}", ex.getLocalizedMessage());
        }
        return subTree;
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

    /**
     * Get the
     * <code>vertex</code> in this graph that is the source of the (single) edge that is incident on this vertex. A
     * vertex on a tree has only one incident edge but several.
     * @param vertex the vertex whose predecessor is to be returned
     * @return a <code>Collection</code> view of the predecessors of <code>vertex</code> in this graph
     */
    public final V getPredecessor(final V vertex) {
        final Collection<E> inEdges = tree.getInEdges(vertex);
        if (inEdges.size() != 1) {
            throw new IllegalArgumentException("Vertex in tree has incorrect number of in edges.");
        }
        return inEdges.iterator().next().source;
    }

    @Override
    public final boolean removeVertex(final V vertex) {
        return tree.removeVertex(vertex);
    }

    @Override
    public final boolean removeEdge(final E edge) {
        return tree.removeEdge(edge);
    }
    
    /**
     * Add a [sub]tree to the current tree.
     * @param subtree the tree to be added.
     * @param node the node at which the tree is to be added.
     * @param connectingEdge the edge that will be used to connect <code>node</code> to the root of the subtree.
     */
    public final void addSubtree(final Tree<V,E> subtree, final V node, final E connectingEdge) {
       TreeUtils.addSubTree(tree, subtree.tree, node, connectingEdge);
    }
    
    @Override
    public final EdgeType getEdgeType() {
        return EdgeType.DIRECTED;
    }

    @Override
    public final void addVertexAttribute(final String name, final String type, final String defaultValue) {
        vertexAttributes.add(new VertexAttribute(name, type, defaultValue));
    }

    @Override
    public final void addEdgeAttribute(final String name, final String type, final String defaultValue) {
        edgeAttributes.add(new EdgeAttribute(name, type, defaultValue));
    }

    @Getter
    private final Collection<VertexAttribute> vertexAttributes = new ArrayList<>();
    @Getter
    private final Collection<EdgeAttribute> edgeAttributes = new ArrayList<>();
    private DelegateTree<V, E> tree;

}
