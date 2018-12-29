package com.huobi.domain.POJOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huobi.domain.enums.OrderSource;
import com.huobi.domain.enums.OrderState;
import com.huobi.domain.enums.OrderType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * created by jacky. 2018/7/21 3:20 PM
 */
@Getter
@Setter
public class Order {
    private String id;
    private String symbol;
    @JsonProperty(value = "account-id")
    private String accountId;
    private BigDecimal amount;
    private BigDecimal price;
    @JsonProperty(value = "created-at")
    private Long createdAt;
    private OrderType type;

    //region deprecate,  火币API，在field相关属性有返回值，在filled相关属性返回null
    @JsonProperty(value = "field-amount")
    private BigDecimal fieldAmount;
    @JsonProperty(value = "field-cash-amount")
    private BigDecimal fieldCashAmount;
    @JsonProperty(value = "field-fees")
    private BigDecimal fieldFees;
    //endregion

    @JsonProperty(value = "filled-amount")
    private BigDecimal filledAmount;
    @JsonProperty(value = "filled-cash-amount")
    private BigDecimal filledCashAmount;
    @JsonProperty(value = "filled-fees")
    private BigDecimal filledFees;

    @JsonProperty(value = "finished-at")
    private String finishedAt;
    @JsonProperty(value = "user-id")
    private String userId;
    private OrderSource source;
    private OrderState state;
    @JsonProperty(value = "canceled-at")
    private Long canceledAt;
    private String exchange;
    private String batch;
}
