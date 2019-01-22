package com.huobi.service.policyImpl;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.POJOs.ContractAccountInfo;
import com.huobi.domain.POJOs.ContractOrderInfo;
import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.enums.OrderStatus;
import com.huobi.domain.enums.Resolution;
import com.huobi.domain.request.ContractOrderInfoRequest;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.ActualOrderHandler;
import com.huobi.service.InitSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.utils.PrintUtil.print;


public class PolicyByMA {
    private HuobiContractAPI huobiContractAPI;
    private String actualPositionStatus;  //实际持仓状态，已经开多或开空
    private String targetPositionStatus;  //目标持仓状态
    private ActualOrderHandler actualOrderHandler;


    public PolicyByMA(InitSystem initSystem, ActualOrderHandler actualOrderHandler) {
        this.huobiContractAPI = initSystem.huobiContractAPI;
        this.actualOrderHandler = actualOrderHandler;
    }

    public void autoTrade() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    //初始化实际持仓状态
                    if (actualPositionStatus == null) {
                        List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
                        String initPositionStatus = null;
                        //查询已经开的空头数量
                        for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
                            if (contractPositionInfo.getSymbol().equals("BTC") && contractPositionInfo
                                    .getContract_type().equals("quarter") && contractPositionInfo.getVolume() > 0) {
                                initPositionStatus = contractPositionInfo.getDirection();
                                break;
                            }
                        }
                        if (initPositionStatus != null) {
                            actualPositionStatus = initPositionStatus;
                        }
                        actualPositionStatus = "empty";
                    }


                    List<Double> MA5DerivativeList = calculateMA5Derivative(10);
                    double MA5DerFirst = MA5DerivativeList.get(0);
                    double MA5DerSecond = MA5DerivativeList.get(1);

                    if (MA5DerSecond < 0 && MA5DerFirst > 0.2 && (MA5DerFirst - MA5DerSecond) > 0.4) {
                        targetPositionStatus = "buy";
                    }
                    if (MA5DerSecond > 0 && MA5DerFirst > 0.2) {
                        targetPositionStatus = "buy";
                    }

                    if (MA5DerSecond > 0 && MA5DerFirst < -0.2 && (MA5DerFirst - MA5DerSecond) < -0.4) {
                        targetPositionStatus = "sell";
                    }
                    if (MA5DerSecond < 0 && MA5DerFirst < -0.2) {
                        targetPositionStatus = "sell";
                    }


                    if (targetPositionStatus.equals("buy") && (!actualPositionStatus.equals("buy"))) {
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
                            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", 0,
                                    positionVolume, "buy", "close", 20, "opponent");
                            long orderID = huobiContractAPI.placeOrder(contractOrderRequest);
                            //等待交易完成
                            while (!checkOrderFinishedOrNot(orderID)) {
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                }
                            }
                        }
                        //计算可开的多头数量
                        double availableMargin = getAvailableMargin("BTC");
                        long openVolume = getMaxOpenVolume(availableMargin * MAX_OPEN_POSITION_PERCENT, "BTC_CQ");
                        //开多
                        if (openVolume > 0) {
                            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", 0,
                                    openVolume, "buy", "open", 20, "opponent");
                            long orderID = huobiContractAPI.placeOrder(contractOrderRequest);
                            //等待交易完成
                            while (!checkOrderFinishedOrNot(orderID)) {
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                }
                            }
                            actualPositionStatus = "buy";
                        }
                    }


                    if (targetPositionStatus.equals("sell") && (!actualPositionStatus.equals("sell"))) {
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
                            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", 0,
                                    positionVolume, "sell", "close", 20, "opponent");
                            long orderID = huobiContractAPI.placeOrder(contractOrderRequest);
                            //等待交易完成
                            while (!checkOrderFinishedOrNot(orderID)) {
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                }
                            }
                        }
                        //计算可开的空头数量
                        double availableMargin = getAvailableMargin("BTC");
                        long openVolume = getMaxOpenVolume(availableMargin * MAX_OPEN_POSITION_PERCENT, "BTC_CQ");
                        //开空
                        if (openVolume > 0) {
                            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", 0,
                                    openVolume, "sell", "open", 20, "opponent");
                            long orderID = huobiContractAPI.placeOrder(contractOrderRequest);
                            //等待交易完成
                            while (!checkOrderFinishedOrNot(orderID)) {
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                }
                            }
                            actualPositionStatus = "sell";
                        }
                    }
                } catch (IllegalStateException e) {
                }
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

    //根据订单号确认订单是否已经完成
    private boolean checkOrderFinishedOrNot(long orderID) {
        ContractOrderInfoRequest orderInfoRequest = new ContractOrderInfoRequest(orderID, "", "BTC");
        ContractOrderInfo orderDetail = huobiContractAPI.getContractOrderInfo(orderInfoRequest).get(0);
        return orderDetail.getStatus() == (OrderStatus.FILLED.getCode());
    }


    //生成MA的导数系列，列表前面为最新的MA
    private List<Double> calculateMA5Derivative(int klineSize) {
        List<Kline> klineList = huobiContractAPI.getKlines("BTC_CQ", Resolution.M15, String.valueOf(klineSize));
        List<Double> MA5List = new ArrayList<>();
        List<Double> MA5DerivativeList = new ArrayList<>();
        //生成MA5List
        for (int j = klineSize; j >= 5; j--) {
            double MA5Total = 0;
            for (int i = j - 1; i >= j - 5; i--) {
                MA5Total = MA5Total + klineList.get(i).getClose();
            }
            MA5List.add(MA5Total / 5);
        }
        //生成MA5DerivativeList
        for (int i = 0; i < MA5List.size() - 1; i++) {
            MA5DerivativeList.add(MA5List.get(i) - MA5List.get(i + 1));
        }
        return MA5DerivativeList;
    }


}


