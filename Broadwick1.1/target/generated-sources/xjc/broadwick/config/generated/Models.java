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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
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
 * <p>Java class for Models complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Models">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="model" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="classname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="parameter" type="{}Parameter" maxOccurs="unbounded"/>
 *                   &lt;element name="priors" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{}Prior">
 *                           &lt;sequence maxOccurs="unbounded">
 *                             &lt;element name="gaussianPrior" type="{}GaussianPrior" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;element name="uniformPrior" type="{}UniformPrior" maxOccurs="unbounded" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
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
@XmlType(name = "Models", propOrder = {
    "model"
})
public class Models
    implements CopyTo, Copyable, Equals, HashCode, ToString
{

    @XmlElement(required = true)
    protected List<Models.Model> model;

    /**
     * Gets the value of the model property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the model property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModel().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Models.Model }
     * 
     * 
     */
    public List<Models.Model> getModel() {
        if (model == null) {
            model = new ArrayList<Models.Model>();
        }
        return this.model;
    }

    public void toString(ToStringBuilder toStringBuilder) {
        {
            List<Models.Model> theModel;
            theModel = this.getModel();
            toStringBuilder.append("model", theModel);
        }
    }

    public String toString() {
        final ToStringBuilder toStringBuilder = new JAXBToStringBuilder(this);
        toString(toStringBuilder);
        return toStringBuilder.toString();
    }

    public void equals(Object object, EqualsBuilder equalsBuilder) {
        if (!(object instanceof Models)) {
            equalsBuilder.appendSuper(false);
            return ;
        }
        if (this == object) {
            return ;
        }
        final Models that = ((Models) object);
        equalsBuilder.append(this.getModel(), that.getModel());
    }

    public boolean equals(Object object) {
        if (!(object instanceof Models)) {
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
        hashCodeBuilder.append(this.getModel());
    }

    public int hashCode() {
        final HashCodeBuilder hashCodeBuilder = new JAXBHashCodeBuilder();
        hashCode(hashCodeBuilder);
        return hashCodeBuilder.toHashCode();
    }

    public Object copyTo(Object target, CopyBuilder copyBuilder) {
        final Models copy = ((target == null)?new Models():((Models) target));
        {
            List<Models.Model> sourceModel;
            sourceModel = this.getModel();
            List<Models.Model> copyModel = ((List<Models.Model> ) copyBuilder.copy(sourceModel));
            copy.model = null;
            List<Models.Model> uniqueModell = copy.getModel();
            uniqueModell.addAll(copyModel);
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
     *         &lt;element name="classname" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="parameter" type="{}Parameter" maxOccurs="unbounded"/>
     *         &lt;element name="priors" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{}Prior">
     *                 &lt;sequence maxOccurs="unbounded">
     *                   &lt;element name="gaussianPrior" type="{}GaussianPrior" maxOccurs="unbounded" minOccurs="0"/>
     *                   &lt;element name="uniformPrior" type="{}UniformPrior" maxOccurs="unbounded" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/extension>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "classname",
        "parameter",
        "priors"
    })
    public static class Model
        implements CopyTo, Copyable, Equals, HashCode, ToString
    {

        @XmlElement(required = true)
        protected String classname;
        @XmlElement(required = true)
        protected List<Parameter> parameter;
        protected Models.Model.Priors priors;
        @XmlAttribute(name = "id")
        protected String id;

        /**
         * Gets the value of the classname property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getClassname() {
            return classname;
        }

        /**
         * Sets the value of the classname property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setClassname(String value) {
            this.classname = value;
        }

        /**
         * Gets the value of the parameter property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the parameter property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getParameter().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Parameter }
         * 
         * 
         */
        public List<Parameter> getParameter() {
            if (parameter == null) {
                parameter = new ArrayList<Parameter>();
            }
            return this.parameter;
        }

        /**
         * Gets the value of the priors property.
         * 
         * @return
         *     possible object is
         *     {@link Models.Model.Priors }
         *     
         */
        public Models.Model.Priors getPriors() {
            return priors;
        }

        /**
         * Sets the value of the priors property.
         * 
         * @param value
         *     allowed object is
         *     {@link Models.Model.Priors }
         *     
         */
        public void setPriors(Models.Model.Priors value) {
            this.priors = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setId(String value) {
            this.id = value;
        }

        public void toString(ToStringBuilder toStringBuilder) {
            {
                String theClassname;
                theClassname = this.getClassname();
                toStringBuilder.append("classname", theClassname);
            }
            {
                List<Parameter> theParameter;
                theParameter = this.getParameter();
                toStringBuilder.append("parameter", theParameter);
            }
            {
                Models.Model.Priors thePriors;
                thePriors = this.getPriors();
                toStringBuilder.append("priors", thePriors);
            }
            {
                String theId;
                theId = this.getId();
                toStringBuilder.append("id", theId);
            }
        }

        public String toString() {
            final ToStringBuilder toStringBuilder = new JAXBToStringBuilder(this);
            toString(toStringBuilder);
            return toStringBuilder.toString();
        }

        public void equals(Object object, EqualsBuilder equalsBuilder) {
            if (!(object instanceof Models.Model)) {
                equalsBuilder.appendSuper(false);
                return ;
            }
            if (this == object) {
                return ;
            }
            final Models.Model that = ((Models.Model) object);
            equalsBuilder.append(this.getClassname(), that.getClassname());
            equalsBuilder.append(this.getParameter(), that.getParameter());
            equalsBuilder.append(this.getPriors(), that.getPriors());
            equalsBuilder.append(this.getId(), that.getId());
        }

        public boolean equals(Object object) {
            if (!(object instanceof Models.Model)) {
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
            hashCodeBuilder.append(this.getClassname());
            hashCodeBuilder.append(this.getParameter());
            hashCodeBuilder.append(this.getPriors());
            hashCodeBuilder.append(this.getId());
        }

        public int hashCode() {
            final HashCodeBuilder hashCodeBuilder = new JAXBHashCodeBuilder();
            hashCode(hashCodeBuilder);
            return hashCodeBuilder.toHashCode();
        }

        public Object copyTo(Object target, CopyBuilder copyBuilder) {
            final Models.Model copy = ((target == null)?new Models.Model():((Models.Model) target));
            {
                String sourceClassname;
                sourceClassname = this.getClassname();
                String copyClassname = ((String) copyBuilder.copy(sourceClassname));
                copy.setClassname(copyClassname);
            }
            {
                List<Parameter> sourceParameter;
                sourceParameter = this.getParameter();
                List<Parameter> copyParameter = ((List<Parameter> ) copyBuilder.copy(sourceParameter));
                copy.parameter = null;
                List<Parameter> uniqueParameterl = copy.getParameter();
                uniqueParameterl.addAll(copyParameter);
            }
            {
                Models.Model.Priors sourcePriors;
                sourcePriors = this.getPriors();
                Models.Model.Priors copyPriors = ((Models.Model.Priors) copyBuilder.copy(sourcePriors));
                copy.setPriors(copyPriors);
            }
            {
                String sourceId;
                sourceId = this.getId();
                String copyId = ((String) copyBuilder.copy(sourceId));
                copy.setId(copyId);
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
         *     &lt;extension base="{}Prior">
         *       &lt;sequence maxOccurs="unbounded">
         *         &lt;element name="gaussianPrior" type="{}GaussianPrior" maxOccurs="unbounded" minOccurs="0"/>
         *         &lt;element name="uniformPrior" type="{}UniformPrior" maxOccurs="unbounded" minOccurs="0"/>
         *       &lt;/sequence>
         *     &lt;/extension>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "gaussianPriorAndUniformPrior"
        })
        public static class Priors
            extends Prior
            implements CopyTo, Copyable, Equals, HashCode, ToString
        {

            @XmlElements({
                @XmlElement(name = "gaussianPrior", type = GaussianPrior.class),
                @XmlElement(name = "uniformPrior", type = UniformPrior.class)
            })
            protected List<Prior> gaussianPriorAndUniformPrior;

            /**
             * Gets the value of the gaussianPriorAndUniformPrior property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the gaussianPriorAndUniformPrior property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getGaussianPriorAndUniformPrior().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link GaussianPrior }
             * {@link UniformPrior }
             * 
             * 
             */
            public List<Prior> getGaussianPriorAndUniformPrior() {
                if (gaussianPriorAndUniformPrior == null) {
                    gaussianPriorAndUniformPrior = new ArrayList<Prior>();
                }
                return this.gaussianPriorAndUniformPrior;
            }

            public void toString(ToStringBuilder toStringBuilder) {
                super.toString(toStringBuilder);
                {
                    List<Prior> theGaussianPriorAndUniformPrior;
                    theGaussianPriorAndUniformPrior = this.getGaussianPriorAndUniformPrior();
                    toStringBuilder.append("gaussianPriorAndUniformPrior", theGaussianPriorAndUniformPrior);
                }
            }

            public String toString() {
                final ToStringBuilder toStringBuilder = new JAXBToStringBuilder(this);
                toString(toStringBuilder);
                return toStringBuilder.toString();
            }

            public void equals(Object object, EqualsBuilder equalsBuilder) {
                if (!(object instanceof Models.Model.Priors)) {
                    equalsBuilder.appendSuper(false);
                    return ;
                }
                if (this == object) {
                    return ;
                }
                super.equals(object, equalsBuilder);
                final Models.Model.Priors that = ((Models.Model.Priors) object);
                equalsBuilder.append(this.getGaussianPriorAndUniformPrior(), that.getGaussianPriorAndUniformPrior());
            }

            public boolean equals(Object object) {
                if (!(object instanceof Models.Model.Priors)) {
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
                super.hashCode(hashCodeBuilder);
                hashCodeBuilder.append(this.getGaussianPriorAndUniformPrior());
            }

            public int hashCode() {
                final HashCodeBuilder hashCodeBuilder = new JAXBHashCodeBuilder();
                hashCode(hashCodeBuilder);
                return hashCodeBuilder.toHashCode();
            }

            public Object copyTo(Object target, CopyBuilder copyBuilder) {
                final Models.Model.Priors copy = ((target == null)?new Models.Model.Priors():((Models.Model.Priors) target));
                super.copyTo(copy, copyBuilder);
                {
                    List<Prior> sourceGaussianPriorAndUniformPrior;
                    sourceGaussianPriorAndUniformPrior = this.getGaussianPriorAndUniformPrior();
                    List<Prior> copyGaussianPriorAndUniformPrior = ((List<Prior> ) copyBuilder.copy(sourceGaussianPriorAndUniformPrior));
                    copy.gaussianPriorAndUniformPrior = null;
                    List<Prior> uniqueGaussianPriorAndUniformPriorl = copy.getGaussianPriorAndUniformPrior();
                    uniqueGaussianPriorAndUniformPriorl.addAll(copyGaussianPriorAndUniformPrior);
                }
                return copy;
            }

            public Object copyTo(Object target) {
                final CopyBuilder copyBuilder = new JAXBCopyBuilder();
                return copyTo(target, copyBuilder);
            }

        }

    }

}