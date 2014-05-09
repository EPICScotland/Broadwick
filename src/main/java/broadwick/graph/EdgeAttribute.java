package broadwick.graph;

import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * A generic edge attribute.
 */
@Data
@SuppressWarnings("all")
public class EdgeAttribute implements Serializable {

    /**
     * Create a edge attribute object setting the value to be the default value.
     * @param name the name of the attribute.
     * @param type the type of the attribute.
     * @param defaultValue the default value for the attribute.
     */
    public EdgeAttribute(String name, Class type, String defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }



    private final String name;
    private final Class type;
    private final String defaultValue;
    @Getter
    @Setter
    private String value;
}