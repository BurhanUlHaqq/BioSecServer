
package org.datacontract.schemas._2004._07.nadra_biometric;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TemplateType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TemplateType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ANSI"/>
 *     &lt;enumeration value="ISO_19794_2"/>
 *     &lt;enumeration value="SAGEM_PKMAT"/>
 *     &lt;enumeration value="SAGEM_PKCOMPV2"/>
 *     &lt;enumeration value="SAGEM_CFV"/>
 *     &lt;enumeration value="RAW_IMAGE"/>
 *     &lt;enumeration value="WSQ"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TemplateType", namespace = "http://schemas.datacontract.org/2004/07/NADRA.Biometric.Verification")
@XmlEnum
public enum TemplateType {

    ANSI("ANSI"),
    ISO_19794_2("ISO_19794_2"),
    SAGEM_PKMAT("SAGEM_PKMAT"),
    @XmlEnumValue("SAGEM_PKCOMPV2")
    SAGEM_PKCOMPV_2("SAGEM_PKCOMPV2"),
    SAGEM_CFV("SAGEM_CFV"),
    RAW_IMAGE("RAW_IMAGE"),
    WSQ("WSQ");
    private final String value;

    TemplateType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TemplateType fromValue(String v) {
        for (TemplateType c: TemplateType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
