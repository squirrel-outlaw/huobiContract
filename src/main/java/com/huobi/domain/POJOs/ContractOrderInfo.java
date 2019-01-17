package com.huobi.domain.POJOs;

import com.huobi.domain.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-7 下午4:09
 */
@Getter
@Setter
public class ContractOrderInfo {
    private String symbol;
    private String contract_type;
    private String contract_code;
    private double volume;
    private double price;
    private String order_price_type;
    private String direction;
    private String offset;
    private int lever_rate;
    private long order_id;
    private long client_order_id;
    private long created_at;
    private long trade_volume;
    private double trade_turnover;
    private double fee;
    private double trade_avg_price;
    private double margin_frozen;
    private double profit;
    private int status;
    private String order_type;
    private String order_source;
}
