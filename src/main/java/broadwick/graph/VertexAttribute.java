package broadwick.graph;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * A generic node attribute.
 */
@Data
@SuppressWarnings("all")
public class VertexAttribute {

    /**
     * Create a node attribute object setting the value to be the default value.
     * @param name the name of the attribute.
     * @param type the type of the attribute.
     * @param defaultValue the default value for the attribute.
     */
    public VertexAttribute(String name, String type, String defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }


    private final String name;
    private final String type;
    private final String defaultValue;
    @Getter
    @Setter
    private String value;
}