package com.huobi.service.policyImpl;

import com.huobi.domain.POJOs.Account;
import com.huobi.domain.POJOs.customerPOJOs.Position;
import com.huobi.domain.POJOs.customerPOJOs.TradeSignal;
import com.huobi.service.DataManager;
import com.huobi.service.Policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.service.BasicFunction.getCurrencyFromSymbol;

/**
 * @Description
 * @Author squirrel
 * @Date 18-10-19 下午4:18
 */
public class PolicyOne extends Policy {

    public PolicyOne(DataManager dataManager) {
        super(dataManager);
    }

    public List<TradeSignal> generateTradeSignalList(List<Account> tradeAccountTestList) {
        List<TradeSignal> buyTradeSignalList = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : dataManager.dataMaps5Seconds[1].entrySet()) {
            //根据交易对的实时价格list，计算出tradepoint
            double tradePoint = calculateTradePointFinal(entry.getKey(), entry.getValue());
            //当tradePoint大于买入条件时，把该TradeSignal加入到buyTradeSignalList中
            if (tradePoint >= BUY_TRADE_POINT) {
                String symbol = entry.getKey();
                String currency = getCurrencyFromSymbol(symbol);


                TradeSignal tradeSignal = new TradeSignal();
                tradeSignal.setSymbol(symbol);
                tradeSignal.setTradePoint(tradePoint);
                buyTradeSignalList.add(tradeSignal);
            }
        }
        return buyTradeSignalList;
    }

    /**
     * @Description: 根据计算和各种修正，得出的最终的TradePoint
     * @param:
     * @return:
     */
    private double calculateTradePointFinal(String symbol, List<Double> realTimePriceDerivativeList) {
        double finalResult = 0d;
        double tradePointByRealTimePriceDerivative = calculateTradePointByRealTimePriceDerivative
                (realTimePriceDerivativeList);
        double tradePointAdjustByPosition = calculateTradePointAdjustByPosition(symbol);
        finalResult = tradePointByRealTimePriceDerivative + tradePointAdjustByPosition;
        return finalResult;
    }


    /**
     * @Description: 根据每个交易对的realTimePriceDerivativeList(实时价格一阶导数列表)，计算出此交易对的TradePoint
     * @param: realTimePriceList  每个交易对的实时价格列表
     * @return:
     */
    private double calculateTradePointByRealTimePriceDerivative(List<Double> realTimePriceDerivativeList) {
        double tradePoint = 0d;
        double realTimePriceDerivativeLast = 0d;     //List中最后一个数据
        double realTimePriceDerivative2rdLast = 0d;  //List中倒数第二个数据
        double realTimePriceDerivative3thLast = 0d;  //List中倒数第三个数据
        if (realTimePriceDerivativeList.size() > 2) {
            realTimePriceDerivativeLast = realTimePriceDerivativeList.get(realTimePriceDerivativeList.size() - 1);
            realTimePriceDerivative2rdLast = realTimePriceDerivativeList.get(realTimePriceDerivativeList.size() - 2);
            realTimePriceDerivative3thLast = realTimePriceDerivativeList.get(realTimePriceDerivativeList.size() - 3);
        }
        tradePoint = realTimePriceDerivativeLast *
                REALTIME_PRICE_DER_COEF_Last + realTimePriceDerivative2rdLast * REALTIME_PRICE_DER_COEF_2RDLAST
                + realTimePriceDerivative3thLast * REALTIME_PRICE_DER_COEF_3THLAST;
        return tradePoint;
    }

    /**
     * @Description: 相关交易对的头寸仓位对 TadePoint的修正
     * @param:
     * @return:
     */
    private double calculateTradePointAdjustByPosition(String symbol) {
        double tradePointAdjustByPosition = 0d;
        for (Position position : dataManager.initSystem.positionList) {
            if (position.getSymbol().equals(symbol)) {
                tradePointAdjustByPosition = position.getPositionPercent() * POSITION_RATE_COEFF;
                return tradePointAdjustByPosition;
            }
        }
        return tradePointAdjustByPosition;
    }


}
