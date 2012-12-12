package broadwick.xml;

import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

/**
 * This simple class can be used to parse the XML string that the framework supplies to the model.
 */
public final class XmlParser {
    
    /**
     * Hiding utility class constructor.
     */
    private XmlParser() {
        
    }

    /**
     * This method unmarshalls (reads) the supplied XML string using JAXB using the supplied class object to create the
     * JAXB context. The class object MUST be valid JAXB compliant and be annotated with @XmlRootElement otherwise
     * a RuntimeException will be thrown.
     * @param xmlStr a bare XML string (with no header information).
     * @param clazz the java class that can be recognized by a JAXBContext. 
     * @return on Object with the same class as the clazz parameter, populated with the elements of the XML.
     * @throws JAXBException if the XML cannot be unmarshalled.
     */
    public static Object unmarshallXmlString(final String xmlStr, final Class clazz) throws JAXBException {

        if (clazz.isAnnotationPresent(XmlRootElement.class)) {

            final String str = new StringBuilder("<?xml version=\"1.0\"?>").append(xmlStr).toString();
            final StringReader xmlReader = new StringReader(str);
            final StreamSource xmlSource = new StreamSource(xmlReader);
            final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(xmlSource, clazz).getValue();
        } else {
            throw new RuntimeException("Cannot unmarshall xml. The supplied class does not correspond to the XML element.");
        }
    }

}
