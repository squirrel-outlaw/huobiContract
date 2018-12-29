package com.huobi.service;

import com.huobi.api.HuobiApiRestClient;
import com.huobi.domain.POJOs.Account;
import com.huobi.domain.POJOs.Order;
import com.huobi.domain.POJOs.customerPOJOs.TradeSignal;
import com.huobi.domain.enums.MergeLevel;
import com.huobi.domain.enums.OrderType;
import com.huobi.domain.enums.PlacingOrderMode;

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
    private HuobiApiRestClient huobiApiRestClient;

    public Policy(DataManager dataManager) {
        this.dataManager = dataManager;
        this.huobiApiRestClient = dataManager.initSystem.huobiApiRestClient;
    }

    //public abstract List<TradeSignal> generateTradeSignalList(List<Account> tradeAccountTestList);

    public void closingVirtualFilledOrder(List<Order> virtualFilledOrderList, TradeSystem tradeSystem) {
        for (Order virtualFilledOrder : virtualFilledOrderList) {
            String symbol = virtualFilledOrder.getSymbol();
            //virtualFilledOrder中的成本价
            double costPrice = virtualFilledOrder.getPrice().doubleValue();
            double highestBuyPrice = huobiApiRestClient.getDepth(symbol, MergeLevel.STEP0).getBids().get(0).get
                    (0);
            if (highestBuyPrice > costPrice * (1 + CLOSING_ORDER_PROFIT_RATE)) {
                Order virtualRequestOrder = new Order();
                virtualRequestOrder.setSymbol(symbol);
                virtualRequestOrder.setAmount(virtualFilledOrder.getAmount());
                virtualRequestOrder.setType(OrderType.SELL_LIMIT);
                tradeSystem.tradeHandling(virtualRequestOrder, PlacingOrderMode.sellSpot, tradeSystem.tradeAccountTestList);
            }
        }
    }

}
