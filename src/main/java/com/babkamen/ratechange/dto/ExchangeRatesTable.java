
package com.babkamen.ratechange.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExchangeRatesTable", propOrder = {
        "date",
        "rates"
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRatesTable {
    /**
     * Format - YYYY-MM-dd
     */
    @XmlElement(name = "EffectiveDate", required = true)
    protected String date;

    @JsonIgnore
    @XmlElement(name = "Rates", required = true)
    protected RatesContainer rates;


}
