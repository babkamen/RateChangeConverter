
package com.babkamen.ratechange.dto;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfExchangeRatesTable", propOrder = {
        "exchangeRatesTable"
})
@Data
public class NbpiResponse {

    @XmlElement(name = "ExchangeRatesTable", required = true)
    protected List<ExchangeRatesTable> exchangeRatesTable;

}
