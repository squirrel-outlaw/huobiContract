package com.huobi.domain.POJOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Symbol {
    private String symbol;
    @JsonProperty("base-currency")
    private String baseCurrency;
    @JsonProperty("quote-currency")
    private String quoteCurrency;
    @JsonProperty("price-precision")
    private String pricePrecision;
    @JsonProperty("amount-precision")
    private String amountPrecision;
    @JsonProperty("symbol-partition")
    private String symbolPartition;
}
