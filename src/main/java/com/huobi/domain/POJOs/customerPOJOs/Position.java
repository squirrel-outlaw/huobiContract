package com.huobi.domain.POJOs.customerPOJOs;

import com.huobi.domain.enums.PositionType;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description  头寸
 * @Author squirrel
 * @Date 18-9-21 上午11:18
 */
@Getter
@Setter
public class Position {
    String symbol;
    PositionType positionType;
    double amount;
    double positionPercent; //仓位占比
}
