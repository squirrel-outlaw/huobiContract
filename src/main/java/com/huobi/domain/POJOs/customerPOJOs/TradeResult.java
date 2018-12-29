package com.huobi.domain.POJOs.customerPOJOs;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description 表示根据交易信号完成的交易的相关结果
 * @Author squirrel
 * @Date 18-9-20 下午12:27
 */
@Getter
@Setter
public class TradeResult {
    private String symbol;
    private long tradeTimestamp;
    private double tradePrice;
    private double Price1MinLater;
    private double profitRate1MinLater;
    private double Price3MinLater;
    private double profitRate3MinLater;
    private double Price5MinLater;
    private double profitRate5MinLater;
    private double Price10MinLater;
    private double profitRate10MinLater;
}
