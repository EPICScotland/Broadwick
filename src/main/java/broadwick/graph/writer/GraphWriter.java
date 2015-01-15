/*
 * Copyright 2014 University of Glasgow.
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

/**
 * Interface defining the writers.
 */
public interface GraphWriter {

    /**
     * Print the content of the graph.
     * @param network the network object to be written.
     * @return a string representing a document.
     */
    String toString(final Graph<? extends Vertex, ? extends Edge<?>> network);

    /**
     * Write the XML document into file.
     * @param network the network object to be written.
     * @param file    the file name
     */
    void save(final String file, final Graph<? extends Vertex, ? extends Edge<Vertex>> network);

}
