
package com.babkamen.ratechange.dto;

import lombok.Data;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RatesContainer", propOrder = {
        "rates"
})
@Data
public class RatesContainer {
    @XmlElement(name = "Rate")
    protected List<NbpiRate> rates;
}
