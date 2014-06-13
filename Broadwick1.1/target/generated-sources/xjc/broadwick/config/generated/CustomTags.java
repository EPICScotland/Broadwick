//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.14 at 11:49:10 AM GMT 
//


package broadwick.config.generated;

import java.util.ArrayList;
import java.util.List;
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
 * <p>Java class for CustomTags complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CustomTags">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="customTag" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="column" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "CustomTags", propOrder = {
    "customTag"
})
public class CustomTags
    implements CopyTo, Copyable, Equals, HashCode, ToString
{

    protected List<CustomTags.CustomTag> customTag;

    /**
     * Gets the value of the customTag property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the customTag property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCustomTag().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CustomTags.CustomTag }
     * 
     * 
     */
    public List<CustomTags.CustomTag> getCustomTag() {
        if (customTag == null) {
            customTag = new ArrayList<CustomTags.CustomTag>();
        }
        return this.customTag;
    }

    public void toString(ToStringBuilder toStringBuilder) {
        {
            List<CustomTags.CustomTag> theCustomTag;
            theCustomTag = this.getCustomTag();
            toStringBuilder.append("customTag", theCustomTag);
        }
    }

    public String toString() {
        final ToStringBuilder toStringBuilder = new JAXBToStringBuilder(this);
        toString(toStringBuilder);
        return toStringBuilder.toString();
    }

    public void equals(Object object, EqualsBuilder equalsBuilder) {
        if (!(object instanceof CustomTags)) {
            equalsBuilder.appendSuper(false);
            return ;
        }
        if (this == object) {
            return ;
        }
        final CustomTags that = ((CustomTags) object);
        equalsBuilder.append(this.getCustomTag(), that.getCustomTag());
    }

    public boolean equals(Object object) {
        if (!(object instanceof CustomTags)) {
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
        hashCodeBuilder.append(this.getCustomTag());
    }

    public int hashCode() {
        final HashCodeBuilder hashCodeBuilder = new JAXBHashCodeBuilder();
        hashCode(hashCodeBuilder);
        return hashCodeBuilder.toHashCode();
    }

    public Object copyTo(Object target, CopyBuilder copyBuilder) {
        final CustomTags copy = ((target == null)?new CustomTags():((CustomTags) target));
        {
            List<CustomTags.CustomTag> sourceCustomTag;
            sourceCustomTag = this.getCustomTag();
            List<CustomTags.CustomTag> copyCustomTag = ((List<CustomTags.CustomTag> ) copyBuilder.copy(sourceCustomTag));
            copy.customTag = null;
            List<CustomTags.CustomTag> uniqueCustomTagl = copy.getCustomTag();
            uniqueCustomTagl.addAll(copyCustomTag);
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
     *         &lt;element name="column" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "column",
        "name",
        "type"
    })
    public static class CustomTag
        implements CopyTo, Copyable, Equals, HashCode, ToString
    {

        protected int column;
        @XmlElement(required = true)
        protected String name;
        @XmlElement(required = true)
        protected String type;

        /**
         * Gets the value of the column property.
         * 
         */
        public int getColumn() {
            return column;
        }

        /**
         * Sets the value of the column property.
         * 
         */
        public void setColumn(int value) {
            this.column = value;
        }

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
         * Gets the value of the type property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setType(String value) {
            this.type = value;
        }

        public void toString(ToStringBuilder toStringBuilder) {
            {
                int theColumn;
                theColumn = this.getColumn();
                toStringBuilder.append("column", theColumn);
            }
            {
                String theName;
                theName = this.getName();
                toStringBuilder.append("name", theName);
            }
            {
                String theType;
                theType = this.getType();
                toStringBuilder.append("type", theType);
            }
        }

        public String toString() {
            final ToStringBuilder toStringBuilder = new JAXBToStringBuilder(this);
            toString(toStringBuilder);
            return toStringBuilder.toString();
        }

        public void equals(Object object, EqualsBuilder equalsBuilder) {
            if (!(object instanceof CustomTags.CustomTag)) {
                equalsBuilder.appendSuper(false);
                return ;
            }
            if (this == object) {
                return ;
            }
            final CustomTags.CustomTag that = ((CustomTags.CustomTag) object);
            equalsBuilder.append(this.getColumn(), that.getColumn());
            equalsBuilder.append(this.getName(), that.getName());
            equalsBuilder.append(this.getType(), that.getType());
        }

        public boolean equals(Object object) {
            if (!(object instanceof CustomTags.CustomTag)) {
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
            hashCodeBuilder.append(this.getColumn());
            hashCodeBuilder.append(this.getName());
            hashCodeBuilder.append(this.getType());
        }

        public int hashCode() {
            final HashCodeBuilder hashCodeBuilder = new JAXBHashCodeBuilder();
            hashCode(hashCodeBuilder);
            return hashCodeBuilder.toHashCode();
        }

        public Object copyTo(Object target, CopyBuilder copyBuilder) {
            final CustomTags.CustomTag copy = ((target == null)?new CustomTags.CustomTag():((CustomTags.CustomTag) target));
            {
                int sourceColumn;
                sourceColumn = this.getColumn();
                int copyColumn = ((int) copyBuilder.copy(sourceColumn));
                copy.setColumn(copyColumn);
            }
            {
                String sourceName;
                sourceName = this.getName();
                String copyName = ((String) copyBuilder.copy(sourceName));
                copy.setName(copyName);
            }
            {
                String sourceType;
                sourceType = this.getType();
                String copyType = ((String) copyBuilder.copy(sourceType));
                copy.setType(copyType);
            }
            return copy;
        }

        public Object copyTo(Object target) {
            final CopyBuilder copyBuilder = new JAXBCopyBuilder();
            return copyTo(target, copyBuilder);
        }

    }

}
