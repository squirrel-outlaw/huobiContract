package com.huobi.service.policyImpl;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.POJOs.ContractAccountInfo;
import com.huobi.domain.POJOs.ContractOrderInfo;
import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.enums.Resolution;
import com.huobi.domain.request.ContractOrderInfoRequest;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.ActualOrderHandler;
import com.huobi.service.InitSystem;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.constant.TradeConditionConsts.HANG_ORDER_INTERVAL;
import static com.huobi.constant.TradeConditionConsts.MAX_OPEN_POSITION_PERCENT;
import static com.huobi.utils.PrintUtil.print;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-24 下午1:54
 */
@Slf4j
public class PolicyWave {
    private HuobiContractAPI huobiContractAPI;
    private String actualPositionStatus;  //实际持仓状态，已经开多或开空
    private String targetPositionStatus;  //目标持仓状态
    private Kline markKline;
    private String markKlineType;
    private boolean isFindMarkKline = false;
    private boolean isThroughMarkKline;
    private double openPositionPrice;
    private List<Long> openPositionHangOrderList = new ArrayList<>();
    private boolean isOpenPositionPriceChange = false;

    public PolicyWave(InitSystem initSystem) {
        this.huobiContractAPI = initSystem.huobiContractAPI;
    }

    public void autoTrade() {
        List<Kline> klineList = huobiContractAPI.getKlines("BTC_CQ", Resolution.M60, "200");
        Kline kline1 = new Kline();
        Kline kline2 = new Kline();
        Kline kline3 = new Kline();
        kline1.setLow(3450);
        kline1.setHigh(3600);
        kline1.setOpen(3500);
        kline1.setClose(3580);
        klineList.add(kline1);
        kline2.setLow(3575);
        kline2.setHigh(3590);
        kline2.setOpen(3580);
        kline2.setClose(3585);
        klineList.add(kline2);
        kline3.setLow(3525);
        kline3.setHigh(3555);
        kline3.setOpen(3550);
        kline3.setClose(3520);
        klineList.add(kline3);

        log.info("系统开始运行");

        for (int i = klineList.size() - 2; i >= 0; i--) {
            double klineRateLength = (klineList.get(i).getClose() - klineList.get(i).getOpen()) * 100 / klineList
                    .get(i).getOpen();

            if (klineRateLength > 1.5) {
                markKline = klineList.get(i);
                markKlineType = "green";
                targetPositionStatus = "short";
                openPositionPrice = klineList.get(i).getClose();
            }

            if (klineRateLength < -1.5) {
                markKline = klineList.get(i);
                ;
                markKlineType = "red";
                targetPositionStatus = "long";
                openPositionPrice = klineList.get(i).getClose();
            }


            for (int j = i + 1; j < klineList.size(); j++) {
                if (klineList.get(j).getClose() > markKline.getHigh()) {
                    targetPositionStatus = "long";
                    openPositionPrice = klineList.get(j).getClose();
                }


                if (targetPositionStatus.equals("short")) {
                    if (klineList.get(j).getClose() * (1 + 0.015) < openPositionPrice) {
                        openPositionPrice = klineList.get(j).getClose() * (1 + 0.015);
                        isOpenPositionPriceChange = true;
                        print("openPositionPrice变化为:" + openPositionPrice);
                    }
                }
                //当后面的某根K线超过之前大阳线0.3以上
                double tempRateLength = (klineList.get(j).getClose() - klineList.get(i).getHigh()) /
                        klineList.get(i).getHigh();
                if (tempRateLength > 0.3) {
                    targetPositionStatus = "long";
                } else {
                    targetPositionStatus = "short";
                }
            }
        }
    }


    Timer timer = new Timer();
        timer.schedule(new

    TimerTask() {
        public void run () {
            try {
                List<Kline> klineNewList = huobiContractAPI.getKlines("BTC_CQ", Resolution.M60, "2");
                Kline newestKline = klineNewList.get(0);
                double klineRateLengthNew = (newestKline.getClose() - newestKline.getOpen()) * 100 / newestKline.getOpen();
                //如果最新又出来一根大阳线或大阴线，更新markKline
                if (klineRateLengthNew > 1.5 && newestKline.getId() > markKline.getId()) {
                    markKline = newestKline;
                    markKlineType = "green";
                    targetPositionStatus = "short";
                    openPositionPrice = klineNewList.get(1).getClose();
                    isOpenPositionPriceChange = true;
                    return;
                }
                if (klineRateLengthNew < -1.5 && newestKline.getId() > markKline.getId()) {
                    markKline = newestKline;
                    markKlineType = "red";
                    targetPositionStatus = "long";
                    openPositionPrice = klineNewList.get(1).getClose();
                    isOpenPositionPriceChange = true;
                    return;
                }


                double newestClosePrice = newestKline.getClose();
                //大阳线后情况
                if (markKlineType.equals("green") && targetPositionStatus.equals("short")) {
                    if (newestClosePrice * (1 + 0.015) < openPositionPrice) {
                        openPositionPrice = newestClosePrice * (1 + 0.015);
                        isOpenPositionPriceChange = true;
                    }
                    //如果最新的K线突破的大阳线的高点
                    if (newestClosePrice > markKline.getHigh()) {
                        targetPositionStatus = "long";
                        openPositionPrice = newestClosePrice;
                        isOpenPositionPriceChange = true;
                    } else {
                        isOpenPositionPriceChange = false;
                    }
                }
                if (markKlineType.equals("green") && targetPositionStatus.equals("long")) {
                    //如果最新的K线又回到大阳线的收盘价之下
                    if (newestClosePrice < markKline.getClose()) {
                        targetPositionStatus = "short";
                        openPositionPrice = newestClosePrice;
                        isOpenPositionPriceChange = true;
                    }

                    if (newestClosePrice * (1 - 0.015) > openPositionPrice) {
                        openPositionPrice = newestClosePrice * (1 - 0.015);
                        isOpenPositionPriceChange = true;
                    } else {
                        isOpenPositionPriceChange = false;
                    }
                }
                //大阴线后情况
                if (markKlineType.equals("red") && targetPositionStatus.equals("long")) {
                    if (newestClosePrice * (1 - 0.015) > openPositionPrice) {
                        openPositionPrice = newestClosePrice * (1 - 0.015);
                        isOpenPositionPriceChange = true;
                    }
                    //如果最新的K线突破的大阴线的低点
                    if (newestClosePrice < markKline.getLow()) {
                        targetPositionStatus = "short";
                        openPositionPrice = newestClosePrice;
                        isOpenPositionPriceChange = true;
                    } else {
                        isOpenPositionPriceChange = false;
                    }
                }

                if (markKlineType.equals("red") && targetPositionStatus.equals("short")) {
                    //如果最新的K线又回到大阴线的收盘价之上
                    if (newestClosePrice > markKline.getClose()) {
                        targetPositionStatus = "long";
                        openPositionPrice = newestClosePrice;
                        isOpenPositionPriceChange = true;
                    }

                    if (newestClosePrice * (1 + 0.015) < openPositionPrice) {
                        openPositionPrice = newestClosePrice * (1 + 0.015);
                        isOpenPositionPriceChange = true;
                    } else {
                        isOpenPositionPriceChange = false;
                    }
                }


                //如果可用的保证金大于百分之10
                if (getAvailableMargin("BTC", true, MARGIN_SAFE_PERCENT) > 0.2) {
                    double availableMargin = getAvailableMargin("BTC", false, MARGIN_SAFE_PERCENT);
                    //long maxOpenVolume = getMaxOpenVolume(availableMargin, "BTC_CQ");
                    long maxOpenVolume = 1;

                    if (targetPositionStatus.equals("short")) {
                        ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", openPositionPrice,
                                maxOpenVolume, "sell", "open", 20, "limit");
                        long orderID = huobiContractAPI.placeOrder(contractOrderRequest);
                        openPositionHangOrderList.add(orderID);
                        print("开仓挂单");
                        print("openPositionHangOrderList:");
                        print(openPositionHangOrderList);
                    }
                    if (targetPositionStatus.equals("long")) {
                        //开多操作
                    }
                }

                //如果开仓价格改变了，撤销所有开仓订单
                if (isOpenPositionPriceChange) {
                    for (long orderID : openPositionHangOrderList) {
                        ContractOrderInfoRequest orderInfoRequest = new ContractOrderInfoRequest(orderID, "", "BTC");
                        huobiContractAPI.cancelOrder(orderInfoRequest);
                    }
                    //清空开仓订单列表
                    openPositionHangOrderList.clear();
                    //重置isOpenPositionPriceChange为false
                    isOpenPositionPriceChange = false;
                }

                //对已有持仓进行平仓挂单
                if (targetPositionStatus.equals("long")) {
                    print("有已有空单进行平仓挂单");
                    closePositionHangOrder("buy");
                }
                if (targetPositionStatus.equals("short")) {
                    print("有已有多单进行平仓挂单");
                    closePositionHangOrder("sell");
                }

            } catch (IllegalStateException e) {
            }
        }
    },0,AUTO_TRADE_INTERVAL);// 取消订单，重新挂单的间隔为2s
}


    //查询可用的保证金
    private double getAvailableMargin(String symbol, boolean isPercent, double safePercent) {
        List<ContractAccountInfo> contractAccountInfoList = huobiContractAPI.getContractAccountInfos();
        for (ContractAccountInfo contractAccountInfo : contractAccountInfoList) {
            if (contractAccountInfo.getSymbol().equals(symbol)) {
                if (isPercent) {
                    return (contractAccountInfo.getMargin_available() - contractAccountInfo.getMargin_balance()
                            * safePercent) / (contractAccountInfo.getMargin_balance()
                            * safePercent);
                }
                return contractAccountInfo.getMargin_available() - contractAccountInfo.getMargin_balance()
                        * safePercent;
            }
        }
        return 0;
    }

    //计算在一定保证金的情况下，最大可开仓量
    private long getMaxOpenVolume(double margin, String contractSymbol) {
        double newestPrice = huobiContractAPI.getTrade(contractSymbol).getPrice();
        return (long) (margin * 20 * newestPrice / 100);
    }

    //查询持仓情况
    private Object queryPosition(String direction, String queryItem) {
        long positionVolume = 0;
        long available = 0;
        double cost_open = 0;
        List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
        for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
            if (contractPositionInfo.getSymbol().equals("BTC") && contractPositionInfo.getContract_type().equals("quarter") && contractPositionInfo.getDirection().equals(direction)) {
                positionVolume = contractPositionInfo.getVolume();
                available = contractPositionInfo.getAvailable();
                cost_open = contractPositionInfo.getCost_open();
            }
        }
        if (queryItem.equals("volume")) {
            return positionVolume;
        }
        if (queryItem.equals("available")) {
            return available;
        }
        if (queryItem.equals("cost_open")) {
            return cost_open;
        }
        return "error";
    }

    //对已有持仓进行平仓挂单
    private void closePositionHangOrder(String direction) {
        long availablePosition = (long) queryPosition(direction, "available");
        print("availablePosition:" + availablePosition);
        if (availablePosition > 0) {
            double cost_open = (double) queryPosition(direction, "cost_open");
            double closePositionPrice1;
            double closePositionPrice2;
            String hangDirection;
            if (direction.equals("buy")) {
                closePositionPrice1 = cost_open * (1 + 0.015);
                closePositionPrice2 = cost_open * (1 + 0.02);
                hangDirection = "sell";
            } else {
                closePositionPrice1 = cost_open * (1 - 0.015);
                closePositionPrice2 = cost_open * (1 - 0.02);
                hangDirection = "buy";
            }
            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", closePositionPrice1,
                    availablePosition / 2, hangDirection, "close", 20, "limit");
            huobiContractAPI.placeOrder(contractOrderRequest);
            contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", closePositionPrice2,
                    availablePosition - availablePosition / 2, hangDirection, "close", 20, "limit");
            huobiContractAPI.placeOrder(contractOrderRequest);
        }
    }


}
