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
import com.huobi.domain.response.CancelOrderResp;
import com.huobi.service.ActualOrderHandler;
import com.huobi.service.InitSystem;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.utils.PrintUtil.print;

@Slf4j
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
        log.info("系统开始运行");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    //初始化实际持仓状态
                    if (actualPositionStatus == null) {
                        List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
                        String initPositionStatus = null;
                        //查询已经是否已经开仓
                        for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
                            if (contractPositionInfo.getSymbol().equals("BTC") && contractPositionInfo
                                    .getContract_type().equals("quarter") && contractPositionInfo.getVolume() > 0) {
                                initPositionStatus = contractPositionInfo.getDirection();
                                break;
                            }
                        }
                        if (initPositionStatus != null) {
                            actualPositionStatus = initPositionStatus;
                        } else {
                            actualPositionStatus = "empty";
                        }
                    }


                    List<Double> MA5DerivativeList = calculateMA5Derivative(10);
                    double MA5DerFirst = MA5DerivativeList.get(0);
                    double MA5DerSecond = MA5DerivativeList.get(1);

                    print("MA5DerFirst:" + MA5DerFirst + "    " + "MA5DerSecond:" + MA5DerSecond);

                    if (MA5DerFirst >= MA_DERIVATIVE_BORDER_LONG_BIG) {
                        targetPositionStatus = "buy";
                    }
                    if (MA5DerFirst >= MA_DERIVATIVE_BORDER_LONG_SMALL && MA5DerFirst < MA_DERIVATIVE_BORDER_LONG_BIG) {
                        if (actualPositionStatus.equals("sell")) {
                            targetPositionStatus = "empty";
                        } else {
                            targetPositionStatus = actualPositionStatus;
                        }
                    }
                    if (MA5DerFirst >= MA_DERIVATIVE_BORDER_SHORT_SMALL && MA5DerFirst < MA_DERIVATIVE_BORDER_LONG_SMALL) {
                        targetPositionStatus = actualPositionStatus;
                    }
                    if (MA5DerFirst >= MA_DERIVATIVE_BORDER_SHORT_BIG && MA5DerFirst < MA_DERIVATIVE_BORDER_SHORT_SMALL) {
                        if (actualPositionStatus.equals("buy")) {
                            targetPositionStatus = "empty";
                        } else {
                            targetPositionStatus = actualPositionStatus;
                        }
                    }
                    if (MA5DerFirst < MA_DERIVATIVE_BORDER_SHORT_BIG) {
                        targetPositionStatus = "sell";
                    }

                    if (targetPositionStatus.equals("buy") && (!actualPositionStatus.equals("buy"))) {
                        //查询空头持仓数量
                        long positionVolume = queryPositionVolume("sell");
                        //平掉空头仓位
                        if (positionVolume > 0) {
                            ContractOrderInfo finishedOrder = finishOrder(positionVolume, "buy", "close", "opponent");
                            log.info("已平掉空头仓位;" + "平仓价为：" + finishedOrder.getPrice() + ";" + "平仓数量为：" + finishedOrder.getTrade_volume());
                            actualPositionStatus="empty";
                        }
                        //计算可开的多头数量
                        double availableMargin = getAvailableMargin("BTC");
                        long openVolume = getMaxOpenVolume(availableMargin * MAX_OPEN_POSITION_PERCENT, "BTC_CQ");
                        //开多
                        if (openVolume > 0) {
                            ContractOrderInfo finishedOrder = finishOrder(openVolume, "buy", "open", "opponent");
                            actualPositionStatus = "buy";
                            log.info("已开多;" + "开仓价为：" + finishedOrder.getPrice() + ";" + "开仓数量为：" + finishedOrder.getTrade_volume());
                        }
                    }


                    if (targetPositionStatus.equals("sell") && (!actualPositionStatus.equals("sell"))) {
                        //查询多头持仓数量
                        long positionVolume = queryPositionVolume("buy");
                        //平掉多头仓位
                        if (positionVolume > 0) {
                            ContractOrderInfo finishedOrder = finishOrder(positionVolume, "sell", "close", "opponent");
                            log.info("已平掉多头仓位;"+"平仓价为："+finishedOrder.getPrice()+";"+"平仓数量为："+finishedOrder.getTrade_volume());
                            actualPositionStatus="empty";
                        }
                        //计算可开的空头数量
                        double availableMargin = getAvailableMargin("BTC");
                        long openVolume = getMaxOpenVolume(availableMargin * MAX_OPEN_POSITION_PERCENT, "BTC_CQ");
                        //开空
                        if (openVolume > 0) {
                            ContractOrderInfo finishedOrder = finishOrder(openVolume, "sell", "open", "opponent");
                            actualPositionStatus = "sell";
                            log.info("已开空;" + "开仓价为：" + finishedOrder.getPrice() + ";" + "开仓数量为：" + finishedOrder.getTrade_volume());
                        }
                    }

                    if (targetPositionStatus.equals("empty")) {
                        //查询多头或空头持仓数量
                        long positionVolumeLong = queryPositionVolume("buy");
                        long positionVolumeShort = queryPositionVolume("sell");

                        //平掉多头仓位
                        if (positionVolumeLong > 0) {
                            ContractOrderInfo finishedOrder = finishOrder(positionVolumeLong, "sell", "close", "opponent");
                            log.info("已平掉多头仓位;"+"平仓价为："+finishedOrder.getPrice()+";"+"平仓数量为："+finishedOrder.getTrade_volume());
                            actualPositionStatus="empty";
                        }
                        //平掉空头仓位
                        if (positionVolumeShort > 0) {
                            ContractOrderInfo finishedOrder = finishOrder(positionVolumeShort, "buy", "close", "opponent");
                            log.info("已平掉空头仓位;"+"平仓价为："+finishedOrder.getPrice()+";"+"平仓数量为："+finishedOrder.getTrade_volume());
                            actualPositionStatus="empty";
                        }
                    }

                } catch (IllegalStateException e) {
                }
            }
        }, 0, HANG_ORDER_INTERVAL);// 取消订单，重新挂单的间隔为2s
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
        for (int j = klineList.size(); j >= 5; j--) {
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

    //查询持仓量
    private long queryPositionVolume(String direction) {
        long positionVolume = 0;
        List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
        for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
            if (contractPositionInfo.getSymbol().equals("BTC") && contractPositionInfo.getContract_type().equals("quarter") && contractPositionInfo.getDirection().equals(direction)) {
                positionVolume = contractPositionInfo.getVolume();
            }
        }
        return positionVolume;
    }


    private ContractOrderInfo finishOrder(long volume, String direction, String offset, String orderPriceType) {
        ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", 0,
                volume, direction, offset, 20, orderPriceType);
        if (orderPriceType.equals("opponent")) {
            long orderID = huobiContractAPI.placeOrder(contractOrderRequest);
            //等待交易完成
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                ContractOrderInfoRequest orderInfoRequest = new ContractOrderInfoRequest(orderID, "", "BTC");
                ContractOrderInfo orderDetail = huobiContractAPI.getContractOrderInfo(orderInfoRequest).get(0);
                if (orderDetail.getStatus() == (OrderStatus.FILLED.getCode())) {
                    return orderDetail;
                }
                if (orderDetail.getStatus() == (OrderStatus.SUBMITTED.getCode())) {
                    huobiContractAPI.cancelOrder(orderInfoRequest);
                }
                if (orderDetail.getStatus() == (OrderStatus.CANCELED.getCode())) {
                    orderID = huobiContractAPI.placeOrder(contractOrderRequest);
                }
            }
        }
        return null;
    }


}


