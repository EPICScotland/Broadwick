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
package broadwick.phylo;

import broadwick.graph.Vertex;
import lombok.Getter;
import lombok.Setter;

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
    @Setter
    private double distance;
}
