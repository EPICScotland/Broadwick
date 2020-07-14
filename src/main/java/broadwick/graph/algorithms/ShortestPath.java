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
package broadwick.graph.algorithms;

import broadwick.graph.Edge;
import broadwick.graph.EdgeType;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import broadwick.graph.Graph;
import broadwick.graph.Vertex;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import java.util.List;
import com.google.common.base.Function;

/**
 * Calculates distances in a specified graph, using Dijkstra's single-source-shortest-path algorithm. All edge weights
 * in the graph must be nonnegative; otherwise an
 * <code>IllegalArgumentException</code> will be thrown.
 * @param <V> the vertex type.
 * @param <E> the edge type.
 */
public class ShortestPath<V extends Vertex, E extends Edge<V>> {

    /**
     * Create a ShortestPath instance.
     * @param graph the network/graph on which we will be looking for shortest paths.
     */
    public ShortestPath(final Graph<V, E> graph) {

        if (null == graph.getEdgeType()) {
            throw new IllegalArgumentException("Could not create ShortestPath object for unknown graph type.");
        } else switch (graph.getEdgeType()) {
            case DIRECTED:
                jungGraph = new edu.uci.ics.jung.graph.DirectedSparseMultigraph<>();
                break;
            case UNDIRECTED:
                jungGraph = new edu.uci.ics.jung.graph.UndirectedSparseMultigraph<>();
                break;
            default:
                throw new IllegalArgumentException("Could not create ShortestPath object for unknown graph type.");
        }
        for (final E edge : graph.getEdges()) {
            jungGraph.addEdge(edge, edge.getSource(), edge.getDestination());
        }
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

        final DijkstraDistance<V, E> distance = new DijkstraDistance<V, E>(jungGraph, weightTransformer );
        final Number d = distance.getDistance(source, target);
        return d == null ? 0 : d.doubleValue();
    }

    /**
     * Returns a List of the edges on the shortest path from source to target, in order of their occurrence on this
     * path. If either vertex is not in the graph for which this instance was created, throws IllegalArgumentException
     * @param source the vertex from which distances are measured
     * @param target the number of vertics for which to measure distances
     * @return a list of edges that comprise the shortest path from source to target.
     */
    public final List<E> getEdgesInPath(final V source, final V target) {
        final DijkstraShortestPath<V,E> path = new DijkstraShortestPath<>(jungGraph);
        return path.getPath(source, target);
    }

    private edu.uci.ics.jung.graph.AbstractTypedGraph<V,E> jungGraph;
    private final Function<E, ? extends Number> weightTransformer = new Function<E, Number>() {
        @Override
        public Double apply(final E edge) {
            // all nonnull values are instances of broadwick.graph.Edge
            if (edge != null) {
                return ((Edge) edge).getWeight();
            } else {
                return 1.0;
            }
        }
    };

}
