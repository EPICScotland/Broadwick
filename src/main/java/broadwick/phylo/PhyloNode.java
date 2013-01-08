package broadwick.phylo;

import broadwick.graph.Vertex;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A node object for phylogenetic trees. Each node contains the id and the distance from the previous branching point.
 */
@EqualsAndHashCode(callSuper=false)
public class PhyloNode extends Vertex {

    /**
     * Create an instance of the node.
     * @param name     the id for the node.
     * @param distance the distance from the previous branching point.
     */
    public PhyloNode(final String name, final double distance) {
        super(name);
        this.distance = distance;
    }

    @Override
    public final String toString() {
        return String.format("<%s:%f>", id, distance);
    }

    @Getter
    private double distance;
}
