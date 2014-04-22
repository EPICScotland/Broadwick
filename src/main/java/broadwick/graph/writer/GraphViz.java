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
package broadwick.graph.writer;

import broadwick.graph.Edge;
import broadwick.graph.EdgeType;
import broadwick.graph.Graph;
import broadwick.graph.Vertex;
import broadwick.io.FileOutput;

/**
 * Simple class to write the nodes/edges of a network in graphviz (dot) format. The static methods here will create a
 * dot file that can be converted with the 'neato' graph manipulation tool.
 * <p>
 * neato -Tpng example.dot -o example.png
 * </p>
 */
public final class GraphViz {

    /**
     * hidden constructor for static class.
     */
    private GraphViz() {
        // no constructor
    }

    /**
     * Print the content of the graph.
     * @param network the network object to be written.
     * @return a string representing a document.
     */
    public static String toString(final Graph<? extends Vertex, ? extends Edge<?>> network) {
        final StringBuilder sb = new StringBuilder();

        final String edgeOp = network.getEdgeType() == EdgeType.DIRECTED ? "->" : "--";
        final String graphHdr = network.getEdgeType() == EdgeType.DIRECTED ? "digraph" : "graph";

        sb.append(graphHdr).append("  {\n");

        for (Vertex vertex : network.getVertices()) {
            sb.append("   ");
            sb.append(vertex.getId());
            sb.append(";\n");
        }

        sb.append("\n");

        for (Edge<?> edge : network.getEdges()) {
            sb.append("   ");
            sb.append(edge.getSource().getId());
            sb.append(" ").append(edgeOp).append(" ");
            sb.append(edge.getDestination().getId());
            sb.append(";\n");
        }

        sb.append("\n}");

        return sb.toString();
    }

    /**
     * Save the graph as in graphviz format in the given file.
     * @param network the network object to be saved.
     * @param file    the name of the file in which the graph will be saved.
     */
    public static void save(final String file, final Graph<? extends Vertex, ? extends Edge<Vertex>> network) {
        try (FileOutput fo = new FileOutput(file)) {
            fo.write(GraphViz.toString(network));
        }
    }
}
