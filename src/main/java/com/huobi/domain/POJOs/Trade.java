package com.huobi.domain.POJOs;

import lombok.Data;

/**
 * created by jacky. 2018/7/21 2:33 PM
 */
@Data
public class Trade {
    private String id;
    private double price;
    private double amount;
    private String direction;
    private long ts;
}
