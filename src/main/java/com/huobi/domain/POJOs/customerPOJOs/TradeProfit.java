package com.huobi.domain.POJOs.customerPOJOs;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: squirrel
 * @Date: 18-9-18 下午2:44
 */
@Getter
@Setter
public class TradeProfit {
    //交易类型
    private String tradeType;
   //是否完成
    private boolean isFilled;
    private double profit;
    //盈亏占这次交易额的百分比
    private double profitRateByTrade;
    //盈亏占总资产的百分比
    private double profitRateByTotal;
}
