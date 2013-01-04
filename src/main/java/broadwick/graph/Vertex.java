package broadwick.graph;

import lombok.Setter;

/**
 * This class represents a generic vertex (node) in a graph.
 */
public class Vertex {

    public Vertex(final String id) {
        this.id = id;
    }

    @Setter
    private String id = null;
    
}