//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.14 at 11:49:10 AM GMT 
//


package broadwick.config.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jvnet.jaxb2_commons.lang.CopyTo;
import org.jvnet.jaxb2_commons.lang.Copyable;
import org.jvnet.jaxb2_commons.lang.Equals;
import org.jvnet.jaxb2_commons.lang.HashCode;
import org.jvnet.jaxb2_commons.lang.ToString;
import org.jvnet.jaxb2_commons.lang.builder.CopyBuilder;
import org.jvnet.jaxb2_commons.lang.builder.JAXBCopyBuilder;
import org.jvnet.jaxb2_commons.lang.builder.JAXBEqualsBuilder;
import org.jvnet.jaxb2_commons.lang.builder.JAXBHashCodeBuilder;
import org.jvnet.jaxb2_commons.lang.builder.JAXBToStringBuilder;


/**
 * <p>Java class for Logs complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Logs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="file" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="level" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="pattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="overwrite" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="console" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="level" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="pattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Logs", propOrder = {
    "file",
    "console"
})
public class Logs
    implements CopyTo, Copyable, Equals, HashCode, ToString
{

    protected Logs.File file;
    protected Logs.Console console;

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link Logs.File }
     *     
     */
    public Logs.File getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link Logs.File }
     *     
     */
    public void setFile(Logs.File value) {
        this.file = value;
    }

    /**
     * Gets the value of the console property.
     * 
     * @return
     *     possible object is
     *     {@link Logs.Console }
     *     
     */
    public Logs.Console getConsole() {
        return console;
    }

    /**
     * Sets the value of the console property.
     * 
     * @param value
     *     allowed object is
     *     {@link Logs.Console }
     *     
     */
    public void setConsole(Logs.Console value) {
        this.console = value;
    }

    public void toString(ToStringBuilder toStringBuilder) {
        {
            Logs.File theFile;
            theFile = this.getFile();
            toStringBuilder.append("file", theFile);
        }
        {
            Logs.Console theConsole;
            theConsole = this.getConsole();
            toStringBuilder.append("console", theConsole);
        }
    }

    public String toString() {
        final ToStringBuilder toStringBuilder = new JAXBToStringBuilder(this);
        toString(toStringBuilder);
        return toStringBuilder.toString();
    }

    public void equals(Object object, EqualsBuilder equalsBuilder) {
        if (!(object instanceof Logs)) {
            equalsBuilder.appendSuper(false);
            return ;
        }
        if (this == object) {
            return ;
        }
        final Logs that = ((Logs) object);
        equalsBuilder.append(this.getFile(), that.getFile());
        equalsBuilder.append(this.getConsole(), that.getConsole());
    }

    public boolean equals(Object object) {
        if (!(object instanceof Logs)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final EqualsBuilder equalsBuilder = new JAXBEqualsBuilder();
        equals(object, equalsBuilder);
        return equalsBuilder.isEquals();
    }

    public void hashCode(HashCodeBuilder hashCodeBuilder) {
        hashCodeBuilder.append(this.getFile());
        hashCodeBuilder.append(this.getConsole());
    }

    public int hashCode() {
        final HashCodeBuilder hashCodeBuilder = new JAXBHashCodeBuilder();
        hashCode(hashCodeBuilder);
        return hashCodeBuilder.toHashCode();
    }

    public Object copyTo(Object target, CopyBuilder copyBuilder) {
        final Logs copy = ((target == null)?new Logs():((Logs) target));
        {
            Logs.File sourceFile;
            sourceFile = this.getFile();
            Logs.File copyFile = ((Logs.File) copyBuilder.copy(sourceFile));
            copy.setFile(copyFile);
        }
        {
            Logs.Console sourceConsole;
            sourceConsole = this.getConsole();
            Logs.Console copyConsole = ((Logs.Console) copyBuilder.copy(sourceConsole));
            copy.setConsole(copyConsole);
        }
        return copy;
    }

    public Object copyTo(Object target) {
        final CopyBuilder copyBuilder = new JAXBCopyBuilder();
        return copyTo(target, copyBuilder);
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="level" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="pattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "level",
        "pattern"
    })
    public static class Console
        implements CopyTo, Copyable, Equals, HashCode, ToString
    {

        @XmlElement(required = true)
        protected String level;
        protected String pattern;

        /**
         * Gets the value of the level property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLevel() {
            return level;
        }

        /**
         * Sets the value of the level property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLevel(String value) {
            this.level = value;
        }

        /**
         * Gets the value of the pattern property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPattern() {
            return pattern;
        }

        /**
         * Sets the value of the pattern property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPattern(String value) {
            this.pattern = value;
        }

        public void toString(ToStringBuilder toStringBuilder) {
            {
                String theLevel;
                theLevel = this.getLevel();
                toStringBuilder.append("level", theLevel);
            }
            {
                String thePattern;
                thePattern = this.getPattern();
                toStringBuilder.append("pattern", thePattern);
            }
        }

        public String toString() {
            final ToStringBuilder toStringBuilder = new JAXBToStringBuilder(this);
            toString(toStringBuilder);
            return toStringBuilder.toString();
        }

        public void equals(Object object, EqualsBuilder equalsBuilder) {
            if (!(object instanceof Logs.Console)) {
                equalsBuilder.appendSuper(false);
                return ;
            }
            if (this == object) {
                return ;
            }
            final Logs.Console that = ((Logs.Console) object);
            equalsBuilder.append(this.getLevel(), that.getLevel());
            equalsBuilder.append(this.getPattern(), that.getPattern());
        }

        public boolean equals(Object object) {
            if (!(object instanceof Logs.Console)) {
                return false;
            }
            if (this == object) {
                return true;
            }
            final EqualsBuilder equalsBuilder = new JAXBEqualsBuilder();
            equals(object, equalsBuilder);
            return equalsBuilder.isEquals();
        }

        public void hashCode(HashCodeBuilder hashCodeBuilder) {
            hashCodeBuilder.append(this.getLevel());
            hashCodeBuilder.append(this.getPattern());
        }

        public int hashCode() {
            final HashCodeBuilder hashCodeBuilder = new JAXBHashCodeBuilder();
            hashCode(hashCodeBuilder);
            return hashCodeBuilder.toHashCode();
        }

        public Object copyTo(Object target, CopyBuilder copyBuilder) {
            final Logs.Console copy = ((target == null)?new Logs.Console():((Logs.Console) target));
            {
                String sourceLevel;
                sourceLevel = this.getLevel();
                String copyLevel = ((String) copyBuilder.copy(sourceLevel));
                copy.setLevel(copyLevel);
            }
            {
                String sourcePattern;
                sourcePattern = this.getPattern();
                String copyPattern = ((String) copyBuilder.copy(sourcePattern));
                copy.setPattern(copyPattern);
            }
            return copy;
        }

        public Object copyTo(Object target) {
            final CopyBuilder copyBuilder = new JAXBCopyBuilder();
            return copyTo(target, copyBuilder);
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="level" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="pattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="overwrite" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "name",
        "level",
        "pattern",
        "overwrite"
    })
    public static class File
        implements CopyTo, Copyable, Equals, HashCode, ToString
    {

        @XmlElement(required = true)
        protected String name;
        @XmlElement(required = true)
        protected String level;
        protected String pattern;
        protected Boolean overwrite;

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }

        /**
         * Gets the value of the level property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLevel() {
            return level;
        }

        /**
         * Sets the value of the level property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLevel(String value) {
            this.level = value;
        }

        /**
         * Gets the value of the pattern property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPattern() {
            return pattern;
        }

        /**
         * Sets the value of the pattern property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPattern(String value) {
            this.pattern = value;
        }

        /**
         * Gets the value of the overwrite property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isOverwrite() {
            return overwrite;
        }

        /**
         * Sets the value of the overwrite property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setOverwrite(Boolean value) {
            this.overwrite = value;
        }

        public void toString(ToStringBuilder toStringBuilder) {
            {
                String theName;
                theName = this.getName();
                toStringBuilder.append("name", theName);
            }
            {
                String theLevel;
                theLevel = this.getLevel();
                toStringBuilder.append("level", theLevel);
            }
            {
                String thePattern;
                thePattern = this.getPattern();
                toStringBuilder.append("pattern", thePattern);
            }
            {
                Boolean theOverwrite;
                theOverwrite = this.isOverwrite();
                toStringBuilder.append("overwrite", theOverwrite);
            }
        }

        public String toString() {
            final ToStringBuilder toStringBuilder = new JAXBToStringBuilder(this);
            toString(toStringBuilder);
            return toStringBuilder.toString();
        }

        public void equals(Object object, EqualsBuilder equalsBuilder) {
            if (!(object instanceof Logs.File)) {
                equalsBuilder.appendSuper(false);
                return ;
            }
            if (this == object) {
                return ;
            }
            final Logs.File that = ((Logs.File) object);
            equalsBuilder.append(this.getName(), that.getName());
            equalsBuilder.append(this.getLevel(), that.getLevel());
            equalsBuilder.append(this.getPattern(), that.getPattern());
            equalsBuilder.append(this.isOverwrite(), that.isOverwrite());
        }

        public boolean equals(Object object) {
            if (!(object instanceof Logs.File)) {
                return false;
            }
            if (this == object) {
                return true;
            }
            final EqualsBuilder equalsBuilder = new JAXBEqualsBuilder();
            equals(object, equalsBuilder);
            return equalsBuilder.isEquals();
        }

        public void hashCode(HashCodeBuilder hashCodeBuilder) {
            hashCodeBuilder.append(this.getName());
            hashCodeBuilder.append(this.getLevel());
            hashCodeBuilder.append(this.getPattern());
            hashCodeBuilder.append(this.isOverwrite());
        }

        public int hashCode() {
            final HashCodeBuilder hashCodeBuilder = new JAXBHashCodeBuilder();
            hashCode(hashCodeBuilder);
            return hashCodeBuilder.toHashCode();
        }

        public Object copyTo(Object target, CopyBuilder copyBuilder) {
            final Logs.File copy = ((target == null)?new Logs.File():((Logs.File) target));
            {
                String sourceName;
                sourceName = this.getName();
                String copyName = ((String) copyBuilder.copy(sourceName));
                copy.setName(copyName);
            }
            {
                String sourceLevel;
                sourceLevel = this.getLevel();
                String copyLevel = ((String) copyBuilder.copy(sourceLevel));
                copy.setLevel(copyLevel);
            }
            {
                String sourcePattern;
                sourcePattern = this.getPattern();
                String copyPattern = ((String) copyBuilder.copy(sourcePattern));
                copy.setPattern(copyPattern);
            }
            {
                Boolean sourceOverwrite;
                sourceOverwrite = this.isOverwrite();
                Boolean copyOverwrite = ((Boolean) copyBuilder.copy(sourceOverwrite));
                copy.setOverwrite(copyOverwrite);
            }
            return copy;
        }

        public Object copyTo(Object target) {
            final CopyBuilder copyBuilder = new JAXBCopyBuilder();
            return copyTo(target, copyBuilder);
        }

    }

}
