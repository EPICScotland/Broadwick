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
import java.util.ArrayList;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

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

    /**
     * Obtain an attribute of this vertex by the attributes name.
     * @param attributeName the name of the attribute to be found.
     * @return the attribute (or null if no attribute matches the name).
     */
    public VertexAttribute getAttributeByName(final String attributeName) {
        for (VertexAttribute attr : attributes) {
            if (StringUtils.equalsIgnoreCase(attributeName, attr.getName())) {
                return attr;
            }
        }
        return null;
    }

    /**
     * Add an attribute to the vertex.
     * @param attribute the attribute to be added.
     * @return true if the collection of attributes changed as a result of the call
     */
    public boolean addAttribute(final VertexAttribute attribute) {
        return attributes.add(attribute);
    }

    public void setXCoord(final double x) {
        xCoord = x;
        attributes.add(new VertexAttribute("xCoord", "Double", String.valueOf(x)));
    }

    public void setYCoord(final double y) {
        yCoord = y;
        attributes.add(new VertexAttribute("yCoord", "Double", String.valueOf(y)));
    }

    @Setter
    @Getter
    protected String id = null;

    @Getter
    protected double xCoord = 0.0;

    @Getter
    protected double yCoord = 0.0;

    @Getter
    private Collection<VertexAttribute> attributes = new ArrayList<>();
}
