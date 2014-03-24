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
import java.io.Serializable;
import java.util.Collection;

/**
 * A graph consisting of a set of vertices of type
 * <code>V</code> set and a set of edges of type
 * <code>E</code>. Edges of this graph type have exactly two endpoints; whether these endpoints must be distinct depends
 * on the implementation. <p> This implementation is shamelessly copied from the Jung graph library which is used as the
 * implementation for all the classes in this package.
 * @param <V> the vertex type.
 * @param <E> the edge type.
 */
public interface Graph<V extends Vertex, E extends Edge<V>> extends Serializable {

    /**
     * Returns a
     * <code>Collection</code> view of the incoming edges incident to
     * <code>vertex</code> in this graph.
     * @param vertex the vertex whose incoming edges are to be returned
     * @return a <code>Collection</code> view of the incoming edges incident to <code>vertex</code> in this graph
     */
    Collection<E> getInEdges(V vertex);

    /**
     * Returns a
     * <code>Collection</code> view of the outgoing edges incident to
     * <code>vertex</code> in this graph.
     * @param vertex the vertex whose outgoing edges are to be returned
     * @return a <code>Collection</code> view of the outgoing edges incident to <code>vertex</code> in this graph
     */
    Collection<E> getOutEdges(V vertex);

    /**
     * Returns a
     * <code>Collection</code> view of the predecessors of
     * <code>vertex</code> in this graph. A predecessor of
     * <code>vertex</code> is defined as a vertex
     * <code>v</code> which is connected to
     * <code>vertex</code> by an edge
     * <code>e</code>, where
     * <code>e</code> is an outgoing edge of
     * <code>v</code> and an incoming edge of
     * <code>vertex</code>.
     * @param vertex the vertex whose predecessors are to be returned
     * @return a <code>Collection</code> view of the predecessors of <code>vertex</code> in this graph
     */
    Collection<V> getPredecessors(V vertex);

    /**
     * Returns a
     * <code>Collection</code> view of the successors of
     * <code>vertex</code> in this graph. A successor of
     * <code>vertex</code> is defined as a vertex
     * <code>v</code> which is connected to
     * <code>vertex</code> by an edge
     * <code>e</code>, where
     * <code>e</code> is an incoming edge of
     * <code>v</code> and an outgoing edge of
     * <code>vertex</code>.
     * @param vertex the vertex whose predecessors are to be returned
     * @return a <code>Collection</code> view of the successors of <code>vertex</code> in this graph
     */
    Collection<V> getSuccessors(V vertex);

    /**
     * Returns the number of incoming edges incident to
     * <code>vertex</code>. Equivalent to
     * <code>getInEdges(vertex).size()</code>.
     * @param vertex the vertex whose indegree is to be calculated
     * @return the number of incoming edges incident to <code>vertex</code>
     */
    int inDegree(V vertex);

    /**
     * Returns the number of outgoing edges incident to
     * <code>vertex</code>. Equivalent to
     * <code>getOutEdges(vertex).size()</code>.
     * @param vertex the vertex whose outdegree is to be calculated
     * @return the number of outgoing edges incident to <code>vertex</code>
     */
    int outDegree(V vertex);

    /**
     * Returns
     * <code>true</code> if
     * <code>v1</code> is a predecessor of
     * <code>v2</code> in this graph. Equivalent to
     * <code>v1.getPredecessors().contains(v2)</code>.
     * @param v1 the first vertex to be queried
     * @param v2 the second vertex to be queried
     * @return <code>true</code> if <code>v1</code> is a predecessor of <code>v2</code>, and false otherwise.
     */
    boolean isPredecessor(V v1, V v2);

    /**
     * Returns
     * <code>true</code> if
     * <code>v1</code> is a successor of
     * <code>v2</code> in this graph. Equivalent to
     * <code>v1.getSuccessors().contains(v2)</code>.
     * @param v1 the first vertex to be queried
     * @param v2 the second vertex to be queried
     * @return <code>true</code> if <code>v1</code> is a successor of <code>v2</code>, and false otherwise.
     */
    boolean isSuccessor(V v1, V v2);

    /**
     * Returns the number of predecessors that
     * <code>vertex</code> has in this graph. Equivalent to
     * <code>vertex.getPredecessors().size()</code>.
     * @param vertex the vertex whose predecessor count is to be returned
     * @return the number of predecessors that <code>vertex</code> has in this graph
     */
    int getPredecessorCount(V vertex);

    /**
     * Returns the number of successors that
     * <code>vertex</code> has in this graph. Equivalent to
     * <code>vertex.getSuccessors().size()</code>.
     * @param vertex the vertex whose successor count is to be returned
     * @return the number of successors that <code>vertex</code> has in this graph
     */
    int getSuccessorCount(V vertex);

    /**
     * If
     * <code>directed_edge</code> is a directed edge in this graph, returns the source; otherwise returns
     * <code>null</code>. The source of a directed edge
     * <code>d</code> is defined to be the vertex for which
     * <code>d</code> is an outgoing edge.
     * <code>directed_edge</code> is guaranteed to be a directed edge if its
     * <code>EdgeType</code> is
     * <code>DIRECTED</code>.
     * @param directedEdge the edge for which we want the corresponding source.
     * @return the source of <code>directed_edge</code> if it is a directed edge in this graph, or <code>null</code>
     *         otherwise
     */
    V getSource(E directedEdge);

    /**
     * If
     * <code>directed_edge</code> is a directed edge in this graph, returns the destination; otherwise returns
     * <code>null</code>. The destination of a directed edge
     * <code>d</code> is defined to be the vertex incident to
     * <code>d</code> for which
     * <code>d</code> is an incoming edge.
     * <code>directed_edge</code> is guaranteed to be a directed edge if its
     * <code>EdgeType</code> is
     * <code>DIRECTED</code>.
     * @param directedEdge the edge for which we want the corresponding destination.
     * @return the destination of <code>directed_edge</code> if it is a directed edge in this graph, *      *         or <code>null</code> otherwise
     */
    V getDest(E directedEdge);

    /**
     * Returns
     * <code>true</code> if
     * <code>vertex</code> is the source of
     * <code>edge</code>. Equivalent to
     * <code>getSource(edge).equals(vertex)</code>.
     * @param vertex the vertex to be queried
     * @param edge   the edge to be queried
     * @return <code>true</code> iff <code>vertex</code> is the source of <code>edge</code>
     */
    boolean isSource(V vertex, E edge);

    /**
     * Returns
     * <code>true</code> if
     * <code>vertex</code> is the destination of
     * <code>edge</code>. Equivalent to
     * <code>getDest(edge).equals(vertex)</code>.
     * @param vertex the vertex to be queried
     * @param edge   the edge to be queried
     * @return <code>true</code> iff <code>vertex</code> is the destination of <code>edge</code>
     */
    boolean isDest(V vertex, E edge);

    /**
     * Adds a vertex to the graph. If the graph is a tree the first node added will be set as the root.
     * @param vertex the vertex to be added to the graph
     * @return true if this call mutates the underlying graph
     */
    boolean addVertex(V vertex);

    /**
     * Adds edge
     * <code>e</code> to this graph such that it connects vertex
     * <code>v1</code> to
     * <code>v2</code>. Equivalent to
     * <code>addEdge(e, new Pair<V>(v1, v2))</code>. If this graph does not contain
     * <code>v1</code>,
     * <code>v2</code>, or both, implementations may choose to either silently add the vertices to the graph or throw an
     * <code>IllegalArgumentException</code>. If this graph assigns edge types to its edges, the edge type of
     * <code>e</code> will be the default for this graph. See
     * <code>Hypergraph.addEdge()</code> for a listing of possible reasons for failure.
     * @param e  the edge to be added
     * @param v1 the first vertex to be connected
     * @param v2 the second vertex to be connected
     * @return <code>true</code> if the add is successful, <code>false</code> otherwise
     * @see Hypergraph#addEdge(Object, Collection)
     * @see #addEdge(Object, Object, Object, EdgeType)
     */
    boolean addEdge(E e, V v1, V v2);

    /**
     * Adds edge
     * <code>e</code> to this graph such that it connects vertex
     * <code>v1</code> to
     * <code>v2</code>. Equivalent to
     * <code>addEdge(e, new Pair<V>(v1, v2))</code>. If this graph does not contain
     * <code>v1</code>,
     * <code>v2</code>, or both, implementations may choose to either silently add the vertices to the graph or throw an
     * <code>IllegalArgumentException</code>. If
     * <code>edgeType</code> is not legal for this graph, this method will throw
     * <code>IllegalArgumentException</code>. See
     * <code>Hypergraph.addEdge()</code> for a listing of possible reasons for failure.
     * @param e        the edge to be added
     * @param v1       the first vertex to be connected
     * @param v2       the second vertex to be connected
     * @param edgeType the type to be assigned to the edge
     * @return <code>true</code> if the add is successful, <code>false</code> otherwise
     * @see Hypergraph#addEdge(Object, Collection)
     * @see #addEdge(Object, Object, Object)
     */
    boolean addEdge(E e, V v1, V v2, EdgeType edgeType);

    /**
     * Returns the endpoints of
     * <code>edge</code> as a
     * <code>Pair<V></code>.
     * @param edge the edge whose endpoints are to be returned
     * @return the endpoints (incident vertices) of <code>edge</code>
     */
    Pair<V, V> getEndpoints(E edge);

    /**
     * Returns the vertex at the other end of
     * <code>edge</code> from
     * <code>vertex</code>. (That is, returns the vertex incident to
     * <code>edge</code> which is not
     * <code>vertex</code>.)
     * @param vertex the vertex to be queried
     * @param edge   the edge to be queried
     * @return the vertex at the other end of <code>edge</code> from <code>vertex</code>
     */
    V getOpposite(V vertex, E edge);

    /**
     * Returns the number of vertices in this graph.
     * @return the number of vertices in this graph
     */
    int getVertexCount();

    /**
     * Returns a view of all vertices in this graph. In general, this obeys the
     * <code>Collection</code> contract, and therefore makes no guarantees about the ordering of the vertices within the
     * set.
     * @return a <code>Collection</code> view of all vertices in this graph
     */
    Collection<V> getVertices();

    /**
     * Returns a view of all edges in this graph. In general, this obeys the
     * <code>Collection</code> contract, and therefore makes no guarantees about the ordering of the edges within the
     * set.
     * @return a <code>Collection</code> view of all edges in this graph
     */
    Collection<E> getEdges();

    /**
     * Remove the passed node, and, in the case of a tree, all nodes that are descendants of the passed node.
     * @param vertex the vertex to be removed.
     * @return <code>true</code> iff the graph was modified
     */
    boolean removeVertex(V vertex);
    
    /**
     * Removes <code>edge</code> from this graph.
     * Fails if <code>edge</code> is null, or is otherwise not an element of this graph.
     * @param edge the edge to remove
     * @return <code>true</code> if the removal is successful, <code>false</code> otherwise
     */
    boolean removeEdge(E edge);

    Collection<VertexAttribute> getVertexAttributes();
    
    Collection<EdgeAttribute> getEdgeAttributes();
    
    /**
     * Add a new node attribute to the network.
     * @param name the name of the attribute.
     * @param type the type of attribute (int string etc).
     * @param defaultValue the default value for the attribute.
     */
    void addVertexAttribute(final String name, final Class type, final String defaultValue);

    /**
     * Add a new node attribute to the network.
     * @param name the name of the attribute.
     * @param type the type of attribute (int string etc).
     * @param defaultValue the default value for the attribute.
     */
    void addEdgeAttribute(final String name, final Class type, final String defaultValue);

    /**
     * Get the type of edge (directed/undirected employed in the graph.
     * @return either EdgeType.DIRECTED or EdgeType.UNDIRECTED
     */
    EdgeType getEdgeType();
}
