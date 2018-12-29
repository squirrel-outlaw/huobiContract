package com.huobi.domain.POJOs.customerPOJOs;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: squirrel
 * @Date: 18-9-19 上午9:12
 */
@Getter
@Setter
public class TradeSignal implements Comparable<TradeSignal> {
    private String symbol;
    private double TradePoint;

    @Override
    public int compareTo(TradeSignal o) {
        if (this.TradePoint > o.TradePoint) {
            return 1;
        } else if (this.TradePoint == o.TradePoint) {
            return 0;
        } else {
            return -1;
        }
    }
}
