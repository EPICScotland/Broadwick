//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.14 at 11:49:10 AM GMT 
//


package broadwick.config.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 * <p>Java class for PopulationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PopulationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="locationIdColumn" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="populationSizeColumn" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="populationDateColumn" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="speciesColumn" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PopulationType", propOrder = {
    "locationIdColumn",
    "populationSizeColumn",
    "populationDateColumn",
    "speciesColumn"
})
public class PopulationType
    implements CopyTo, Copyable, Equals, HashCode, ToString
{

    protected int locationIdColumn;
    protected int populationSizeColumn;
    protected int populationDateColumn;
    protected Integer speciesColumn;

    /**
     * Gets the value of the locationIdColumn property.
     * 
     */
    public int getLocationIdColumn() {
        return locationIdColumn;
    }

    /**
     * Sets the value of the locationIdColumn property.
     * 
     */
    public void setLocationIdColumn(int value) {
        this.locationIdColumn = value;
    }

    /**
     * Gets the value of the populationSizeColumn property.
     * 
     */
    public int getPopulationSizeColumn() {
        return populationSizeColumn;
    }

    /**
     * Sets the value of the populationSizeColumn property.
     * 
     */
    public void setPopulationSizeColumn(int value) {
        this.populationSizeColumn = value;
    }

    /**
     * Gets the value of the populationDateColumn property.
     * 
     */
    public int getPopulationDateColumn() {
        return populationDateColumn;
    }

    /**
     * Sets the value of the populationDateColumn property.
     * 
     */
    public void setPopulationDateColumn(int value) {
        this.populationDateColumn = value;
    }

    /**
     * Gets the value of the speciesColumn property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSpeciesColumn() {
        return speciesColumn;
    }

    /**
     * Sets the value of the speciesColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSpeciesColumn(Integer value) {
        this.speciesColumn = value;
    }

    public void toString(ToStringBuilder toStringBuilder) {
        {
            int theLocationIdColumn;
            theLocationIdColumn = this.getLocationIdColumn();
            toStringBuilder.append("locationIdColumn", theLocationIdColumn);
        }
        {
            int thePopulationSizeColumn;
            thePopulationSizeColumn = this.getPopulationSizeColumn();
            toStringBuilder.append("populationSizeColumn", thePopulationSizeColumn);
        }
        {
            int thePopulationDateColumn;
            thePopulationDateColumn = this.getPopulationDateColumn();
            toStringBuilder.append("populationDateColumn", thePopulationDateColumn);
        }
        {
            Integer theSpeciesColumn;
            theSpeciesColumn = this.getSpeciesColumn();
            toStringBuilder.append("speciesColumn", theSpeciesColumn);
        }
    }

    public String toString() {
        final ToStringBuilder toStringBuilder = new JAXBToStringBuilder(this);
        toString(toStringBuilder);
        return toStringBuilder.toString();
    }

    public void equals(Object object, EqualsBuilder equalsBuilder) {
        if (!(object instanceof PopulationType)) {
            equalsBuilder.appendSuper(false);
            return ;
        }
        if (this == object) {
            return ;
        }
        final PopulationType that = ((PopulationType) object);
        equalsBuilder.append(this.getLocationIdColumn(), that.getLocationIdColumn());
        equalsBuilder.append(this.getPopulationSizeColumn(), that.getPopulationSizeColumn());
        equalsBuilder.append(this.getPopulationDateColumn(), that.getPopulationDateColumn());
        equalsBuilder.append(this.getSpeciesColumn(), that.getSpeciesColumn());
    }

    public boolean equals(Object object) {
        if (!(object instanceof PopulationType)) {
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
        hashCodeBuilder.append(this.getLocationIdColumn());
        hashCodeBuilder.append(this.getPopulationSizeColumn());
        hashCodeBuilder.append(this.getPopulationDateColumn());
        hashCodeBuilder.append(this.getSpeciesColumn());
    }

    public int hashCode() {
        final HashCodeBuilder hashCodeBuilder = new JAXBHashCodeBuilder();
        hashCode(hashCodeBuilder);
        return hashCodeBuilder.toHashCode();
    }

    public Object copyTo(Object target, CopyBuilder copyBuilder) {
        final PopulationType copy = ((target == null)?new PopulationType():((PopulationType) target));
        {
            int sourceLocationIdColumn;
            sourceLocationIdColumn = this.getLocationIdColumn();
            int copyLocationIdColumn = ((int) copyBuilder.copy(sourceLocationIdColumn));
            copy.setLocationIdColumn(copyLocationIdColumn);
        }
        {
            int sourcePopulationSizeColumn;
            sourcePopulationSizeColumn = this.getPopulationSizeColumn();
            int copyPopulationSizeColumn = ((int) copyBuilder.copy(sourcePopulationSizeColumn));
            copy.setPopulationSizeColumn(copyPopulationSizeColumn);
        }
        {
            int sourcePopulationDateColumn;
            sourcePopulationDateColumn = this.getPopulationDateColumn();
            int copyPopulationDateColumn = ((int) copyBuilder.copy(sourcePopulationDateColumn));
            copy.setPopulationDateColumn(copyPopulationDateColumn);
        }
        {
            Integer sourceSpeciesColumn;
            sourceSpeciesColumn = this.getSpeciesColumn();
            Integer copySpeciesColumn = ((Integer) copyBuilder.copy(sourceSpeciesColumn));
            copy.setSpeciesColumn(copySpeciesColumn);
        }
        return copy;
    }

    public Object copyTo(Object target) {
        final CopyBuilder copyBuilder = new JAXBCopyBuilder();
        return copyTo(target, copyBuilder);
    }

}
