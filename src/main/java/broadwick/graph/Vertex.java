package broadwick.graph;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This class represents a generic vertex (node) in a graph.
 */
public class Vertex implements Serializable {

    /**
     * A generic vertex for a graph object.
     * @param id an id for the vertex.
     */
    public Vertex(final String id) {
        this.id = id;
    }

    @Setter
    @Getter
    protected String id = null;
}