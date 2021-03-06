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

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This class represents a generic edge in a graph.
 * @param <V> the type of vertices on the edges. 
 */
public class Edge<V extends Vertex>  implements Serializable {

    /**
     * A generic edge for a graph object.
     * @param id an id for the edge.
     */
    public Edge(final String id) {
        this.id = id;
    }

    /**
     * A generic edge for a graph object.
     * @param id          an id for the edge.
     * @param source      the source of the edge.
     * @param destination the destination of the edge.
     */
    public Edge(final String id, final V source, final V destination) {
        this(id, source, destination, 0.0);

    }

    /**
     * A generic edge for a graph object.
     * @param source      the source of the edge.
     * @param destination the destination of the edge.
     */
    public Edge(final V source, final V destination) {
        this(String.format("%s-%s", source.getId(), destination.getId()), source, destination, 0.0);
    }

    /**
     * A generic edge for a graph object.
     * @param source      the source of the edge.
     * @param destination the destination of the edge.
     * @param weight      the weight attached to the edge.
     */
    public Edge(final V source, final V destination, final Double weight) {
        this(String.format("%s-%s", source.getId(), destination.getId()), source, destination, weight);
    }

    /**
     * A generic edge for a graph object.
     * @param id          an id for the edge.
     * @param source      the source of the edge.
     * @param destination the destination of the edge.
     * @param weight      the weight attached to the edge.
     */
    public Edge(final String id, final V source, final V destination, final Double weight) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }
    
    @Override
    public String toString() {
        return String.format("%s->%s", source.getId(), destination.getId());
    }

    @Getter
    protected String id = null;
    @Getter
    protected V source = null;
    @Getter
    protected V destination = null;
    @Setter
    @Getter
    protected Double weight = 1.0;
}
