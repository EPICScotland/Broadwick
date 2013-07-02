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
