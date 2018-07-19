/*
 * Copyright 2016 University of Glasgow.
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
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

/**
 * Simple class to write the nodes/edges of a network in Newick format.
 */
public class GraphNewick {

    public static String toString(final Graph<? super Vertex, ? extends Edge<?>> tree) {

        StringBuilder sb = new StringBuilder();
        sb.append("(");

        Iterable<? extends Vertex> roots = Iterables.filter(tree.getVertices(), new Predicate<Vertex>() {
                                                        @Override
                                                        public boolean apply(final Vertex node) {
                                                            return tree.getPredecessorCount(node) == 0;
                                                        }
                                                    });

        for (Vertex root : roots) {
            sb.append(asNewick(tree, root));
        }
        sb.append(");");
        return sb.toString();
    }

    public static void save(final String file, final Graph<? super Vertex, ? extends Edge<Vertex>> tree) {
        try (FileOutput fo = new FileOutput(file)) {
            fo.write(GraphNewick.toString(tree));
        }
    }

    private static String asNewick(Graph<Vertex, ? extends Edge<?>> tree, Vertex root) {

        if (tree.getSuccessors(root).isEmpty()) {
            return root.getId();
        } else {
            String output = root.getId();
            output += ",(";

            for (Vertex node : tree.getSuccessors(root)) {
                output += asNewick(tree, node);
                output += ",";
            }
            output = StringUtils.removeEnd(output, ",");
            output += ")";
            return output;
        }
    }
}
