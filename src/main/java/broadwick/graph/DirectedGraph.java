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
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import lombok.Getter;

/**
 * A <code>DirectedGraph</code>, suitable for sparse graphs, that permits parallel edges..
 * <p/>
 * The implementation here is backed by the DelegateTree implementation in the JUNG library.
 * @param <V> the vertex type.
 * @param <E> the edge type.
 */
public class DirectedGraph<V extends Vertex, E extends Edge<V>> implements broadwick.graph.Graph<V, E> {

    /**
     * Creates an instance of the graph.
     */
    public DirectedGraph() {
        graph = new DirectedSparseMultigraph<>();
        vertexmaps = new HashMap<>();
        edgemaps = new HashMap<>();
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
        vertexmaps.put(vertex.getId(), vertex);
        return graph.addVertex(vertex);
    }

    @Override
    public final boolean addEdge(final E e, final V v1, final V v2) {
        edgemaps.put(e.getId(), e);
        return graph.addEdge(e, v1, v2);
    }

    @Override
    public final boolean addEdge(final E e, final V v1, final V v2, final EdgeType edgeType) {
        edgemaps.put(e.getId(), e);
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
    public final V getVertex(final String id) {
        return vertexmaps.get(id);
    }

    @Override
    public final E getEdge(final String id) {
        return edgemaps.get(id);
    }

    @Override
    public final Collection<E> getEdges() {
        return graph.getEdges();
    }

    @Override
    public final boolean removeVertex(final V vertex) {
        final boolean removed = graph.removeVertex(vertex);
        if (removed) {
            vertexmaps.remove(vertex.id);
        }
        return removed;
    }

    @Override
    public final boolean removeEdge(final E edge) {
        final boolean removed = graph.removeEdge(edge);
        if (removed) {
            edgemaps.remove(edge.id);
        }
        return removed;
    }

    @Override
    public final EdgeType getEdgeType() {
        return EdgeType.DIRECTED;
    }

    @Override
    public final void addVertexAttribute(final String name, final Class type, final String defaultValue) {
        vertexAttributes.add(new VertexAttribute(name, type, defaultValue));
    }

    @Override
    public final void addEdgeAttribute(final String name, final Class type, final String defaultValue) {
        edgeAttributes.add(new EdgeAttribute(name, type, defaultValue));
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        graph.getEdges().clear();
        graph.getVertices().clear();
        vertexAttributes.clear();
        edgeAttributes.clear();
        vertexmaps.clear();
        edgemaps.clear();
    }

    @Getter
    private final Collection<VertexAttribute> vertexAttributes = new ArrayList<>();
    @Getter
    private final Collection<EdgeAttribute> edgeAttributes = new ArrayList<>();
    private final DirectedSparseMultigraph<V, E> graph;
    private final HashMap<String, V> vertexmaps;
    private final HashMap<String, E> edgemaps;

}
