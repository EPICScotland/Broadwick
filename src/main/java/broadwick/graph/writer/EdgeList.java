/*
 * Copyright 2015 University of Glasgow.
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
package broadwick.graph.writer;

import broadwick.graph.Edge;
import broadwick.graph.Graph;
import broadwick.graph.Vertex;
import broadwick.io.FileOutput;

/**
 * Simple class to write the nodes/edges of a network in edgelist format.
 */
public final class EdgeList {

    /**
     * hidden constructor for static class.
     */
    private EdgeList() {
        // no constructor
    }

    /**
     * Print the content of the graph.
     * @param network the network object to be written.
     * @return a string representing a document.
     */
    public static String toString(final Graph<? extends Vertex, ? extends Edge<?>> network) {
        final StringBuilder sb = new StringBuilder();

        for (final Edge<?> edge : network.getEdges()) {
            sb.append(edge.getSource().getId()).append("\t");
            sb.append(edge.getDestination().getId()).append("\t");
            sb.append(edge.getWeight()).append("\n");
        }
        return sb.toString();

    }

    /**
     * Save the graph as in edge list format in the given file.
     * @param network the network object to be saved.
     * @param file    the name of the file in which the graph will be saved.
     */
    public static void save(final String file, final Graph<? extends Vertex, ? extends Edge<Vertex>> network) {
        try (FileOutput fo = new FileOutput(file)) {
            fo.write(EdgeList.toString(network));
        }
    }
}
