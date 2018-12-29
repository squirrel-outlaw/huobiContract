package com.huobi.service;

import com.huobi.domain.POJOs.customerPOJOs.Position;
import com.huobi.domain.POJOs.customerPOJOs.TradeSignal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.utils.PrintUtil.print;

/**
 * @Description 根据数据，经过各种策略的计算，获得交易信号
 * @Author squirrel
 * @Date 18-9-19 下午4:27
 */
public class PolicyManger {
    private DataManager dataManager;
    //符合买入条件的TradeSignal列表
    public List<TradeSignal> buyTradeSignalList = new ArrayList<>();
    public List<TradeSignal> sellTradeSignalList = new ArrayList<>();

    public PolicyManger(DataManager dataManager) {
        this.dataManager = dataManager;
    }


    /**
     * @param
     * @return
     * @Description 根据计算得到的tradePoint, 更新buyTradeSignalList
     */
    public void updateBuyTradeSignalList() {
        for (Map.Entry<String, List<Double>> entry : dataManager.dataMaps5Seconds[1].entrySet()) {
            TradeSignal tradeSignal = new TradeSignal();
            tradeSignal.setSymbol(entry.getKey());
            //根据交易对的实时价格list，计算出tradepoint
            double tradePoint = calculateTradePointFinal(entry.getKey(), entry.getValue());
            tradeSignal.setTradePoint(tradePoint);
            //判断buyTradeSignalList中是否已经了该交易对的TradeSignal
            boolean isExistSameSymbolTradeSignal = false;
            TradeSignal tradeSignalExisted = new TradeSignal();
            if (null != buyTradeSignalList) {
                for (TradeSignal tradeSignalTemp : buyTradeSignalList) {
                    if (tradeSignal.getSymbol().equals(tradeSignalTemp.getSymbol())) {
                        isExistSameSymbolTradeSignal = true;
                        tradeSignalExisted = tradeSignalTemp;
                        break;
                    }
                }
            }
            //当tradePoint大于买入条件时，把该TradeSignal加入到buyTradeSignalList中
            if (tradePoint >= BUY_TRADE_POINT) {
                if (isExistSameSymbolTradeSignal) {
                    //如果buyTradeSignalList中已经存在相同的交易对，则移除原来的
                    buyTradeSignalList.remove(tradeSignalExisted);
                }
                //加入最新的
                buyTradeSignalList.add(tradeSignal);
                //如果tradePoint小于买入条件并且buyTradeSignalList中已经存在相同的交易对，移除该交易对的TradeSignal
            } else if (isExistSameSymbolTradeSignal) {
                buyTradeSignalList.remove(tradeSignalExisted);
            }
        }
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


