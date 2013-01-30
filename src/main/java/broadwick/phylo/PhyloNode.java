package broadwick.phylo;

import broadwick.graph.Vertex;
import lombok.Getter;

/**
 * A node object for phylogenetic trees. Each node contains the id and the distance from the previous branching point.
 */
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

    @Override
    public final boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof PhyloNode)) {
            return false;
        }

        final PhyloNode otherNode = (PhyloNode) other;

        return this.id.equals(otherNode.id);// && this.distance == otherNode.distance;
    }
    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 61 * hash + (int) (Double.doubleToLongBits(this.distance) ^ (Double.doubleToLongBits(this.distance) >>> 32));
        return hash;
    }
    

    @Getter
    private double distance;
}
