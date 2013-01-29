/*
 * Copyright 2013 .
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

import lombok.Getter;
import lombok.Setter;

/**
 * This class represents a generic edge in a graph.
 */
public class Edge {

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
    public Edge(final String id, final Vertex source, final Vertex destination) {
        this(id, source, destination, 0.0);

    }

    /**
     * A generic edge for a graph object.
     * @param source      the source of the edge.
     * @param destination the destination of the edge.
     */
    public Edge(final Vertex source, final Vertex destination) {
        this(String.format("%s-%s", source.getId(), destination.getId()), source, destination, 0.0);
    }

    /**
     * A generic edge for a graph object.
     * @param source      the source of the edge.
     * @param destination the destination of the edge.
     * @param weight      the weight attached to the edge.
     */
    public Edge(final Vertex source, final Vertex destination, final Double weight) {
        this(String.format("%s-%s", source.getId(), destination.getId()), source, destination, weight);
    }

    /**
     * A generic edge for a graph object.
     * @param id          an id for the edge.
     * @param source      the source of the edge.
     * @param destination the destination of the edge.
     * @param weight      the weight attached to the edge.
     */
    public Edge(final String id, final Vertex source, final Vertex destination, final Double weight) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    @Getter
    protected String id = null;
    @Getter
    protected Vertex source = null;
    @Getter
    protected Vertex destination = null;
    @Setter
    @Getter
    protected Double weight = 0.0;
}
