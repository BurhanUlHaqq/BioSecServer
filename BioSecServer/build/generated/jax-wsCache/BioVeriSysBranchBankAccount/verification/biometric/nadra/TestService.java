
package verification.biometric.nadra;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.datacontract.schemas._2004._07.nadra_biometric.TemplateType;


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
 *         &lt;element name="type" type="{http://schemas.datacontract.org/2004/07/NADRA.Biometric.Verification}TemplateType" minOccurs="0"/>
 *         &lt;element name="tempType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "type",
    "tempType"
})
@XmlRootElement(name = "TestService")
public class TestService {

    protected TemplateType type;
    @XmlElementRef(name = "tempType", namespace = "http://NADRA.Biometric.Verification", type = JAXBElement.class, required = false)
    protected JAXBElement<String> tempType;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link TemplateType }
     *     
     */
    public TemplateType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link TemplateType }
     *     
     */
    public void setType(TemplateType value) {
        this.type = value;
    }

    /**
     * Gets the value of the tempType property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTempType() {
        return tempType;
    }

    /**
     * Sets the value of the tempType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTempType(JAXBElement<String> value) {
        this.tempType = value;
    }

}
