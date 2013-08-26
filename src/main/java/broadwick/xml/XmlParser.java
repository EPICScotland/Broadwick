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
package broadwick.xml;

import broadwick.BroadwickException;
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
    public static Object unmarshallXmlString(final String xmlStr, final Class<?> clazz) throws JAXBException {

        if (clazz.isAnnotationPresent(XmlRootElement.class)) {

            final String str = new StringBuilder("<?xml version=\"1.0\"?>").append(xmlStr).toString();
            final StringReader xmlReader = new StringReader(str);
            final StreamSource xmlSource = new StreamSource(xmlReader);
            final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(xmlSource, clazz).getValue();
        } else {
            throw new BroadwickException("Cannot unmarshall xml. The supplied class does not correspond to the XML element.");
        }
    }

}
