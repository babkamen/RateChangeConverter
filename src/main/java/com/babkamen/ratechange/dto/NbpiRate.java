
package com.babkamen.ratechange.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Rate", propOrder = {
        "code",
        "value"
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NbpiRate {
    /**
     * Currency code e.g USD,...
     */
    @XmlElement(name = "Code", required = true)
    protected String code;
    /**
     * Rate value
     */
    @XmlElement(name = "Mid", required = true)
    protected double value;

}
