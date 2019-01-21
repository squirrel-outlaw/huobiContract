package com.huobi.service.policyImpl;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.POJOs.ContractAccountInfo;
import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.enums.Resolution;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.ActualOrderHandler;
import com.huobi.service.InitSystem;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.utils.PrintUtil.print;


public class PolicyByMA {
    private HuobiContractAPI huobiContractAPI;
    private double lastMA_Diff = 0d;
    private ActualOrderHandler actualOrderHandler;

    public PolicyByMA(InitSystem initSystem, ActualOrderHandler actualOrderHandler) {
        this.huobiContractAPI = initSystem.huobiContractAPI;
        this.actualOrderHandler = actualOrderHandler;
    }

    public void autoTrade() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                double MA_Diff = calculateMA_Diff();
                print("MA_Diff:" + MA_Diff);
                if (lastMA_Diff == 0) {
                    lastMA_Diff = MA_Diff;
                    return;
                }

                if (lastMA_Diff < MA_BORDER_CORRECTION_UP && MA_Diff > MA_BORDER_CORRECTION_UP) {
                    List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
                    long positionVolume = 0;
                    //查询已经开的空头数量
                    for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
                        if (contractPositionInfo.getSymbol().equals("BTC") && contractPositionInfo.getContract_type().equals("quarter") && contractPositionInfo.getDirection().equals("sell")) {
                            positionVolume = contractPositionInfo.getVolume();
                        }
                    }
                    //平掉空头仓位
                    if (positionVolume > 0) {
                        actualOrderHandler.actualRequestOrder = new ContractOrderRequest("BTC", "quarter", null, "", 0,
                                positionVolume, "buy", "close", 20, "limit");
                    }
                    //等待交易完成
                    while (actualOrderHandler.actualRequestOrder != null) {
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                        }
                    }
                    //计算可开的多头数量
                    double availableMargin = getAvailableMargin("BTC");
                    long openVolume = getMaxOpenVolume(availableMargin * MAX_OPEN_POSITION_PERCENT, "BTC_CQ");
                    //开多
                    if (openVolume > 0) {
                        actualOrderHandler.actualRequestOrder = new ContractOrderRequest("BTC", "quarter", null, "", 0,
                                openVolume, "buy", "open", 20, "limit");
                    }
                    print(actualOrderHandler.actualRequestOrder);

                    //等待交易完成
                    while (actualOrderHandler.actualRequestOrder != null) {
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                        }
                    }
                }


                if (lastMA_Diff > MA_BORDER_CORRECTION_DOWN && MA_Diff < MA_BORDER_CORRECTION_DOWN) {
                    List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
                    long positionVolume = 0;
                    //查询已经开的多头数量
                    for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
                        if (contractPositionInfo.getSymbol().equals("BTC") && contractPositionInfo.getContract_type().equals("quarter") && contractPositionInfo.getDirection().equals("buy")) {
                            positionVolume = contractPositionInfo.getVolume();
                        }
                    }
                    //平掉多头仓位
                    if (positionVolume > 0) {
                        actualOrderHandler.actualRequestOrder = new ContractOrderRequest("BTC", "quarter", null, "", 0,
                                positionVolume, "sell", "close", 20, "limit");
                    }
                    //等待交易完成
                    while (actualOrderHandler.actualRequestOrder != null) {
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                        }
                    }
                    //计算可开的空头数量
                    double availableMargin = getAvailableMargin("BTC");

                    long openVolume = getMaxOpenVolume(availableMargin * MAX_OPEN_POSITION_PERCENT, "BTC_CQ");
                    //开空
                    if (openVolume > 0) {
                        actualOrderHandler.actualRequestOrder = new ContractOrderRequest("BTC", "quarter", null, "", 0,
                                openVolume, "sell", "open", 20, "limit");
                    }
                    //等待交易完成
                    while (actualOrderHandler.actualRequestOrder != null) {
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                        }
                    }

                }
                lastMA_Diff = MA_Diff;

            }
        }, 0, HANG_ORDER_INTERVAL);// 取消订单，重新挂单的间隔为2s
    }


    private double calculateMA_Diff() {
        List<Kline> klineList = huobiContractAPI.getKlines("BTC_CQ", Resolution.M15, "10");
        double MA5Total = 0;
        double MA10Total = 0;
        for (int i = klineList.size() - 1; i >= 0; i--) {
            if (i >= 5) {
                MA5Total = MA5Total + klineList.get(i).getClose();
            }
            MA10Total = MA10Total + klineList.get(i).getClose();
        }
        return (MA5Total / 5 - MA10Total / 10);
    }


    //查询可用的保证金
    private double getAvailableMargin(String symbol) {
        List<ContractAccountInfo> contractAccountInfoList = huobiContractAPI.getContractAccountInfos();
        for (ContractAccountInfo contractAccountInfo : contractAccountInfoList) {
            if (contractAccountInfo.getSymbol().equals(symbol)) {
                return contractAccountInfo.getMargin_available();
            }
        }
        return 0;
    }


    private long getMaxOpenVolume(double margin, String contractSymbol) {
        double newestPrice = huobiContractAPI.getTrade(contractSymbol).getPrice();
        return (long) (margin * 20 * newestPrice / 100);
    }

}


