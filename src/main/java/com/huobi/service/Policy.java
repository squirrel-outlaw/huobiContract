package com.huobi.service;


import com.huobi.domain.POJOs.Account;
import com.huobi.domain.POJOs.Order;
import com.huobi.domain.POJOs.customerPOJOs.TradeSignal;
import com.huobi.domain.enums.MergeLevel;
import com.huobi.domain.enums.OrderType;
import com.huobi.domain.enums.PlacingOrderMode;
import com.huobi.domain.request.ContractOrderRequest;

import java.math.BigDecimal;
import java.util.List;

import static com.huobi.constant.TradeConditionConsts.CLOSING_ORDER_PROFIT_RATE;

/**
 * @Description
 * @Author squirrel
 * @Date 18-10-19 下午4:03
 */
public abstract class Policy {

    public DataManager dataManager;

    public Policy(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public abstract ContractOrderRequest generateContractOrderRequest();

}
