package com.huobi.service.policyImpl;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.POJOs.ContractAccountInfo;
import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.enums.Resolution;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.InitSystem;
import com.huobi.service.Policy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.huobi.constant.TradeConditionConsts.*;

import static com.huobi.utils.PrintUtil.print;


/**
 * @Description 根据K线的上引线或下引线来获利
 * @Author squirrel
 * @Date 19-1-29 上午8:51
 */
public class PolicyByLead extends Policy {

    private String currentKlineStatus = "";
    private String currentOpenningStatus = "normal";
    private boolean currentOpenningStatusForbidLock = false;

    private String currentClosingStatusBuy = "normal";
    private String currentClosingStatusSell = "normal";

    private boolean isLastHourHasLongPosition = false;
    private boolean isLastHourHasShortPosition = false;

    private long isLastHourHasLongPositionStartTimeStamp;
    private long isLastHourHasShortPositionStartTimeStamp;

    private double marginSafePercent = POLICYBYLEAD_MARGIN_SAFE_RATE_INIT;

    private double openPositionHangPriceRateSmall;
    private double openPositionHangPriceRateMiddle;
    private double openPositionHangPriceRateBig;

    private double closePositionHangPriceRateSmall;
    private double closePositionHangPriceRateMiddle;
    private double closePositionHangPriceRateBig;

    public PolicyByLead(InitSystem initSystem, boolean isThisPolicyAvailable) {
        super(initSystem, isThisPolicyAvailable);
    }

    @Override
    public void autoTrade() {
        policyStartInfo = df.format(new Date()) + " " + "系统开始运行";
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                currentPolicyRunningStatus = df.format(new Date()) + " " + "系统正常运行中";

                List<Kline> klineList = huobiContractAPI.getKlines("BTC_CQ", Resolution.M60, "3");
                double lastKlineLengthRate = (klineList.get(1).getClose() - klineList.get(1).getOpen()) * 100 / klineList
                        .get(1).getOpen();
                double secondLastKlineLengthRate = (klineList.get(0).getClose() - klineList.get(0).getOpen()) * 100 /
                        klineList.get(0).getOpen();
                if (lastKlineLengthRate > 1.5 || lastKlineLengthRate < -1.5) {
                    currentPolicyRunningStatus = df.format(new Date()) + " " + "上一个小时的K线是大K线,禁止开仓";
                    // 当上一个小时的K线是大K线时禁止开仓
                    currentOpenningStatus = "forbid";
                } else if (secondLastKlineLengthRate > 1 && !currentOpenningStatusForbidLock) {
                    currentOpenningStatus = "carefulLong";
                } else if (secondLastKlineLengthRate < -1 && !currentOpenningStatusForbidLock) {
                    currentOpenningStatus = "carefulShort";
                } else if (!currentOpenningStatusForbidLock) {
                    currentOpenningStatus = "normal";
                }

                double newestKlineLengthRate = (klineList.get(2).getClose() - klineList.get(2).getOpen()) * 100 /
                        klineList.get(2).getOpen();
                if (newestKlineLengthRate > 0) {
                    if (!currentKlineStatus.equals("green")) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "当前K线由红转绿，清除所有订单；";
                        finishCancelAllOrders();
                    }
                    currentKlineStatus = "green";
                    if (currentOpenningStatus.equals("carefulLong")) {
                        openPositionHangPriceRateSmall = klineList.get(2).getClose() * (1 + OPEN_POSITION_RATE_SMALL + 0.5 * 0.01);
                        openPositionHangPriceRateMiddle = klineList.get(2).getClose() * (1 + OPEN_POSITION_RATE_MIDDLE + 0.5 * 0.01);
                        openPositionHangPriceRateBig = klineList.get(2).getClose() * (1 + OPEN_POSITION_RATE_BIG + 0.5 * 0.01);
                    } else {
                        openPositionHangPriceRateSmall = klineList.get(2).getClose() * (1 + OPEN_POSITION_RATE_SMALL);
                        openPositionHangPriceRateMiddle = klineList.get(2).getClose() * (1 + OPEN_POSITION_RATE_MIDDLE);
                        openPositionHangPriceRateBig = klineList.get(2).getClose() * (1 + OPEN_POSITION_RATE_BIG);
                    }
                }
                if (newestKlineLengthRate < 0) {
                    if (!currentKlineStatus.equals("red")) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "当前K线由绿转红，清除所有订单；";
                        finishCancelAllOrders();
                    }
                    currentKlineStatus = "red";
                    if (currentOpenningStatus.equals("carefulLong")) {
                        openPositionHangPriceRateSmall = klineList.get(2).getClose() * (1 - OPEN_POSITION_RATE_SMALL - 0.5 * 0.01);
                        openPositionHangPriceRateMiddle = klineList.get(2).getClose() * (1 - OPEN_POSITION_RATE_MIDDLE - 0.5 * 0.01);
                        openPositionHangPriceRateBig = klineList.get(2).getClose() * (1 - OPEN_POSITION_RATE_BIG - 0.5 * 0.01);
                    } else {
                        openPositionHangPriceRateSmall = klineList.get(2).getClose() * (1 - OPEN_POSITION_RATE_SMALL);
                        openPositionHangPriceRateMiddle = klineList.get(2).getClose() * (1 - OPEN_POSITION_RATE_MIDDLE);
                        openPositionHangPriceRateBig = klineList.get(2).getClose() * (1 - OPEN_POSITION_RATE_BIG);
                    }
                }

                long currentTime = System.currentTimeMillis();
                long currentKlineStartTime = klineList.get(2).getId() * 1000;
                if (currentTime < currentKlineStartTime + 15 * 1000) {
                    currentPolicyRunningStatus = df.format(new Date()) + " " + "最新1小时K线刚刚开始15秒，清除所有订单；";
                    finishCancelAllOrders();
                    long positionVolumeBuy = (long) queryPosition("buy", "volume");
                    long positionVolumeSell = (long) queryPosition("sell", "volume");
                    if (positionVolumeBuy > 0) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "最新1小时K线刚刚开始15秒，发现上一个小时还有多头持仓，清理此持仓";
                        //给下面的forbid状态上锁
                        currentOpenningStatusForbidLock = true;
                        currentOpenningStatus = "forbid";
                        isLastHourHasLongPosition = true;
                        isLastHourHasLongPositionStartTimeStamp = currentTime;
                    }
                    if (positionVolumeSell > 0) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "最新1小时K线刚刚开始15秒，发现上一个小时还有空头持仓，清理此持仓；";
                        //给下面的forbid状态上锁
                        currentOpenningStatusForbidLock = true;
                        currentOpenningStatus = "forbid";
                        isLastHourHasShortPosition = true;
                        isLastHourHasShortPositionStartTimeStamp = currentTime;
                    }
                }

                if (isLastHourHasLongPosition || isLastHourHasShortPosition) {
                    long positionVolumeBuy = (long) queryPosition("buy", "volume");
                    long positionVolumeSell = (long) queryPosition("sell", "volume");
                    currentTime = System.currentTimeMillis();
                    long timeDiff = currentTime - isLastHourHasLongPositionStartTimeStamp;
                    if (positionVolumeBuy > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_SMALL && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE && currentClosingStatusBuy.equals("normal")) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "上个1小时多头持仓15分钟未清理完成，清仓状态变为 urgent；";
                        currentClosingStatusBuy = "urgent";
                        finishCancelAllOrders();
                    }
                    if (positionVolumeBuy > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG && currentClosingStatusBuy.equals("urgent")) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "上个1小时多头持仓30分钟未清理完成，清仓状态变为 " + "veryUrgent；";
                        currentClosingStatusBuy = "veryUrgent";
                        finishCancelAllOrders();
                    }
                    if (positionVolumeBuy > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG && currentClosingStatusBuy.equals("veryUrgent")) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "上个1小时多头持仓45分钟未清理完成，清仓状态变为 mostUrgent；";
                        currentClosingStatusBuy = "mostUrgent";
                        finishCancelAllOrders();
                    }

                    timeDiff = currentTime - isLastHourHasShortPositionStartTimeStamp;
                    if (positionVolumeSell > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_SMALL && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE && currentClosingStatusSell.equals("normal")) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "上个1小时空头持仓15分钟未清理完成，清仓状态变为 urgent；";
                        currentClosingStatusSell = "urgent";
                        finishCancelAllOrders();
                    }
                    if (positionVolumeSell > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG && currentClosingStatusSell.equals("urgent")) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "上个1小时空头持仓30分钟未清理完成，清仓状态变为 " + "veryUrgent；";
                        currentClosingStatusSell = "veryUrgent";
                        finishCancelAllOrders();
                    }
                    if (positionVolumeSell > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG && currentClosingStatusSell.equals("veryUrgent")) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "上个1小时空头持仓45分钟未清理完成，清仓状态变为 mostUrgent；";
                        currentClosingStatusSell = "mostUrgent";
                        finishCancelAllOrders();
                    }
                } else {
                    //解锁之前的forbid状态
                    currentOpenningStatusForbidLock = false;
                    currentClosingStatusBuy = "normal";
                    currentClosingStatusSell = "normal";
                }

                //如果可用的保证金比例大于0.05,并且currentOpenningStatus不为forbid，才进行开仓挂单
                if (getAvailableMargin("BTC", true, marginSafePercent) > 0.05 && !currentOpenningStatus.equals("forbid")) {
                    double availableMargin = getAvailableMargin("BTC", false, marginSafePercent);
                    long maxOpenVolume = getMaxOpenVolume(availableMargin, "BTC_CQ");
                    String hangDirection;
                    long hangVolumeRateSmall;
                    long hangVolumeRateMiddle = 0;
                    long hangVolumeRateBig = 0;
                    if (currentKlineStatus.equals("green")) {
                        hangDirection = "sell";
                    } else {
                        hangDirection = "buy";
                    }
                    double availableMarginPercent = getAvailableMargin("BTC", true, marginSafePercent);
                    if (availableMarginPercent > 0.7) {
                        hangVolumeRateSmall = (long) (maxOpenVolume * 0.3) + 1;
                        hangVolumeRateMiddle = (long) (maxOpenVolume * 0.4) + 1;
                        hangVolumeRateBig = maxOpenVolume - hangVolumeRateSmall - hangVolumeRateMiddle;
                    } else if (availableMarginPercent > 0.33) {
                        hangVolumeRateSmall = (long) (maxOpenVolume * 0.5);
                        hangVolumeRateMiddle = maxOpenVolume - hangVolumeRateSmall;
                    } else {
                        hangVolumeRateSmall = maxOpenVolume;
                    }
                    openClosePositionHang(hangVolumeRateSmall, openPositionHangPriceRateSmall, hangDirection, "open");
                    openClosePositionHang(hangVolumeRateMiddle, openPositionHangPriceRateMiddle, hangDirection, "open");
                    openClosePositionHang(hangVolumeRateBig, openPositionHangPriceRateBig, hangDirection, "open");
                }


                //平仓挂单
                if (currentClosingStatusBuy.equals("normal")) {
                    double cost_hold = (double) queryPosition("buy", "cost_hold");
                    if (cost_hold > 0) {
                        closePositionHangPriceRateSmall = cost_hold * (1 + TAKE_PROFIT_RATE_SMALL);
                        closePositionHangPriceRateMiddle = cost_hold * (1 + TAKE_PROFIT_RATE_MIDDLE);
                        closePositionHangPriceRateBig = cost_hold * (1 + TAKE_PROFIT_RATE_BIG);
                        closePositionHangOrder("buy");
                    }
                }
                if (currentClosingStatusBuy.equals("urgent")) {
                    double cost_hold = (double) queryPosition("buy", "cost_hold");
                    if (cost_hold > 0) {
                        closePositionHangPriceRateSmall = cost_hold * (1 + TAKE_PROFIT_RATE_SMALL - 0.2 * 0.01);
                        closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                        closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                        closePositionHangOrder("buy");
                    }
                }
                if (currentClosingStatusBuy.equals("veryUrgent")) {
                    double cost_hold = (double) queryPosition("buy", "cost_hold");
                    if (cost_hold > 0) {
                        closePositionHangPriceRateSmall = cost_hold * (1 + TAKE_PROFIT_RATE_SMALL - 0.3 * 0.01);
                        closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                        closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                        closePositionHangOrder("buy");
                    }
                }
                if (currentClosingStatusBuy.equals("mostUrgent")) {
                    double newestPrice = huobiContractAPI.getTrade("BTC_CQ").getPrice();
                    double cost_hold = (double) queryPosition("buy", "cost_hold");
                    if (cost_hold > 0) {
                        closePositionHangPriceRateSmall = newestPrice;
                        closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                        closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                        closePositionHangOrder("buy");
                    }
                }
                if (currentClosingStatusSell.equals("normal")) {
                    double cost_hold = (double) queryPosition("sell", "cost_hold");
                    if (cost_hold > 0) {
                        closePositionHangPriceRateSmall = cost_hold * (1 - TAKE_PROFIT_RATE_SMALL);
                        closePositionHangPriceRateMiddle = cost_hold * (1 - TAKE_PROFIT_RATE_MIDDLE);
                        closePositionHangPriceRateBig = cost_hold * (1 - TAKE_PROFIT_RATE_BIG);
                        closePositionHangOrder("sell");
                    }
                }
                if (currentClosingStatusSell.equals("urgent")) {
                    double cost_hold = (double) queryPosition("sell", "cost_hold");
                    if (cost_hold > 0) {
                        closePositionHangPriceRateSmall = cost_hold * (1 - TAKE_PROFIT_RATE_SMALL + 0.2 * 0.01);
                        closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                        closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                        closePositionHangOrder("sell");
                    }
                }
                if (currentClosingStatusSell.equals("veryUrgent")) {
                    double cost_hold = (double) queryPosition("sell", "cost_hold");
                    if (cost_hold > 0) {
                        closePositionHangPriceRateSmall = cost_hold * (1 - TAKE_PROFIT_RATE_SMALL + 0.3 * 0.01);
                        closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                        closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                        closePositionHangOrder("sell");
                    }
                }
                if (currentClosingStatusSell.equals("mostUrgent")) {
                    double newestPrice = huobiContractAPI.getTrade("BTC_CQ").getPrice();
                    double cost_hold = (double) queryPosition("sell", "cost_hold");
                    if (cost_hold > 0) {
                        closePositionHangPriceRateSmall = newestPrice;
                        closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                        closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                        closePositionHangOrder("buy");
                    }
                }

            }
        }, 0, POLICY_BYLEAD_AUTO_TRADE_INTERVAL);// 自动交易间隔时间

    }

    //*********************************************************************************************************************
    //*********************************************************************************************************************

    private void openClosePositionHang(long volume, double price, String direction, String offset) {
        if (volume > 0) {
            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", price,
                    volume, direction, offset, 20, "limit");
            huobiContractAPI.placeOrder(contractOrderRequest);
            currentPolicyRunningStatus = df.format(new Date()) + " " + offset + " 挂单，价格：" + price + ",数量：" + volume +
                    ", 方向：" + direction;
            openClosePositionHangStatusList.add(currentPolicyRunningStatus);
        }
    }


    //对已有持仓进行平仓挂单
    private void closePositionHangOrder(String direction) {
        long availablePositionVolume = (long) queryPosition(direction, "available");
        if (availablePositionVolume > 0) {
            //撤销所有委托订单
            //finishCancelAllOrders();
            //重新获得最新的可用的持仓
            //availablePositionVolume = (long) queryPosition(direction, "available");
            String hangDirection;
            long hangVolumeRateSmall;
            long hangVolumeRateMiddle = 0;
            long hangVolumeRateBig = 0;
            if (direction.equals("buy")) {
                hangDirection = "sell";
            } else {
                hangDirection = "buy";
            }
            double totalMargin = getTotalMargin("BTC");
            long maxOpenVolumeBySafePercent = getMaxOpenVolume(totalMargin * POLICYBYLEAD_MARGIN_SAFE_RATE_INIT, "BTC_CQ");
            double availablePositionVolumePercent = availablePositionVolume / maxOpenVolumeBySafePercent;
            if (availablePositionVolumePercent > 0.7) {
                hangVolumeRateSmall = (long) (availablePositionVolume * 0.4);
                hangVolumeRateMiddle = (long) (availablePositionVolume * 0.3);
                hangVolumeRateBig = availablePositionVolume - hangVolumeRateSmall - hangVolumeRateMiddle;
            } else if (availablePositionVolumePercent > 0.33) {
                hangVolumeRateSmall = (long) (availablePositionVolume * 0.5);
                hangVolumeRateMiddle = availablePositionVolume - hangVolumeRateSmall;
            } else {
                hangVolumeRateSmall = availablePositionVolume;
            }
            openClosePositionHang(hangVolumeRateSmall, closePositionHangPriceRateSmall, hangDirection, "close");
            openClosePositionHang(hangVolumeRateMiddle, closePositionHangPriceRateMiddle, hangDirection, "close");
            openClosePositionHang(hangVolumeRateBig, closePositionHangPriceRateBig, hangDirection, "close");
        }
    }

}
