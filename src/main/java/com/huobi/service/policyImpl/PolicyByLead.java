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

import static com.huobi.utils.ListUtil.fixListLength;
import static com.huobi.utils.PrintUtil.print;


/**
 * @Description 根据K线的上引线或下引线来获利
 * @Author squirrel
 * @Date 19-1-29 上午8:51
 */
public class PolicyByLead extends Policy {
    private String currentKlineStatus = "";
    private String currentOpenningStatus = "normal";
    private boolean openForbidLockByLastHourPositionUnclose = false;
    private boolean openForbidLockByTodayKlineLengthLimit = false;
    private boolean openForbidLockByForceClose = false;

    private String currentClosingStatusBuy = "normal";
    private String currentClosingStatusSell = "normal";

    private boolean isLastHourHasLongPosition = false;
    private boolean isLastHourHasShortPosition = false;
    private long isLastHourHasLongPositionStartTimeStamp;
    private long isLastHourHasShortPositionStartTimeStamp;
    private long lastHourHasLongPositionVolume = 0;  //在上一个小时多头仓的开仓量
    private long lastHourHasShortPositionVolume = 0; //在上一个小时空头仓的开仓量

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
        finishCancelAllOrders();
        Timer timer = new Timer();
        //交易循环开始
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    currentPolicyRunningStatus = df.format(new Date()) + " " + "系统正常运行中";
                    //当天涨跌幅超过一定幅度时不开仓
                    List<Kline> klineDayList = huobiContractAPI.getKlines("BTC_CQ", Resolution.D1, "1");
                    double todayKlineLengthRate = (klineDayList.get(0).getClose() - klineDayList.get(0).getOpen()) * 100 / klineDayList.get(0).getOpen();
                    if (todayKlineLengthRate > TODAY_KLINE_LENGTH_RATE_UP_LIMIT || todayKlineLengthRate < TODAY_KLINE_LENGTH_RATE_DOWN_LIMIT) {
                        currentOpenningStatus = "forbid";
                        openForbidLockByTodayKlineLengthLimit = true;
                    } else {
                        openForbidLockByTodayKlineLengthLimit = false;
                    }

                    List<Kline> klineList = huobiContractAPI.getKlines("BTC_CQ", Resolution.M60, "3");
                    double lastKlineLengthRate = (klineList.get(1).getClose() - klineList.get(1).getOpen()) * 100 / klineList.get(1).getOpen();
                    double secondLastKlineLengthRate = (klineList.get(0).getClose() - klineList.get(0).getOpen()) * 100 / klineList.get(0).getOpen();
                    if (lastKlineLengthRate > 1.5 || lastKlineLengthRate < -1.5) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "上一个小时的K线是大K线,禁止开仓";
                        // 当上一个小时的K线是大K线时禁止开仓
                        currentOpenningStatus = "forbid";
                    } else if (secondLastKlineLengthRate > 1 && !openForbidLockByLastHourPositionUnclose && !openForbidLockByTodayKlineLengthLimit && !openForbidLockByForceClose) {
                        currentOpenningStatus = "carefulLong";
                    } else if (secondLastKlineLengthRate < -1 && !openForbidLockByLastHourPositionUnclose && !openForbidLockByTodayKlineLengthLimit && !openForbidLockByForceClose) {
                        currentOpenningStatus = "carefulShort";
                    } else if (!openForbidLockByLastHourPositionUnclose && !openForbidLockByTodayKlineLengthLimit && !openForbidLockByForceClose) {
                        currentOpenningStatus = "normal";
                    }

                    double newestKlineLengthRate = (klineList.get(2).getClose() - klineList.get(2).getOpen()) * 100 /
                            klineList.get(2).getOpen();
                    if (newestKlineLengthRate > 0) {
                        if (!currentKlineStatus.equals("green")) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "当前K线由红转绿，清除开仓订单；";
                            cancelOrdersAccordingOrderIdList(openPositionHangOrderIDList);
                        }
                        currentKlineStatus = "green";
                        if (currentOpenningStatus.equals("carefulLong")) {
                            openPositionHangPriceRateSmall = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_SMALL + OPEN_POSITION_RATE_CAREFUL_LONG_ADJUST);
                            openPositionHangPriceRateMiddle = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_MIDDLE + OPEN_POSITION_RATE_CAREFUL_LONG_ADJUST);
                            openPositionHangPriceRateBig = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_BIG + OPEN_POSITION_RATE_CAREFUL_LONG_ADJUST);
                        } else {
                            openPositionHangPriceRateSmall = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_SMALL);
                            openPositionHangPriceRateMiddle = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_MIDDLE);
                            openPositionHangPriceRateBig = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_BIG);
                        }
                    }
                    if (newestKlineLengthRate < 0) {
                        if (!currentKlineStatus.equals("red")) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "当前K线由绿转红，清除开仓订单；";
                            cancelOrdersAccordingOrderIdList(openPositionHangOrderIDList);
                        }
                        currentKlineStatus = "red";
                        if (currentOpenningStatus.equals("carefulLong")) {
                            openPositionHangPriceRateSmall = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_SMALL - OPEN_POSITION_RATE_CAREFUL_SHORT_ADJUST);
                            openPositionHangPriceRateMiddle = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_MIDDLE - OPEN_POSITION_RATE_CAREFUL_SHORT_ADJUST);
                            openPositionHangPriceRateBig = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_BIG - OPEN_POSITION_RATE_CAREFUL_SHORT_ADJUST);
                        } else {
                            openPositionHangPriceRateSmall = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_SMALL);
                            openPositionHangPriceRateMiddle = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_MIDDLE);
                            openPositionHangPriceRateBig = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_BIG);
                        }
                    }

                    long currentTime = System.currentTimeMillis();
                    long currentKlineStartTime = klineList.get(2).getId() * 1000;
                    if (currentTime < currentKlineStartTime + WHOLE_TIME_STATUS_SWITCH_CONTINUED) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "最新1小时K线刚刚开始20秒，清除所有订单；";
                        //清除所有订单，做一次初始化的过程
                        finishCancelAllOrders();
                        lastHourHasLongPositionVolume = (long) queryPosition("buy", "volume");
                        lastHourHasShortPositionVolume = (long) queryPosition("sell", "volume");
                        if (lastHourHasLongPositionVolume > 0) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "最新1小时K线刚刚开始15秒，发现上一个小时还有多头持仓，清理此持仓";
                            //cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
                            //给下面的forbid状态上锁
                            openForbidLockByLastHourPositionUnclose = true;
                            currentOpenningStatus = "forbid";
                            isLastHourHasLongPosition = true;
                            isLastHourHasLongPositionStartTimeStamp = currentTime;
                        }
                        if (lastHourHasShortPositionVolume > 0) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "最新1小时K线刚刚开始15秒，发现上一个小时还有空头持仓，清理此持仓；";
                            //cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
                            //给下面的forbid状态上锁
                            openForbidLockByLastHourPositionUnclose = true;
                            currentOpenningStatus = "forbid";
                            isLastHourHasShortPosition = true;
                            isLastHourHasShortPositionStartTimeStamp = currentTime;
                        }
                    }
                    //如果有上一个小时没有平仓的多头持仓
                    if (isLastHourHasLongPosition) {
                        long positionVolumeLong = (long) queryPosition("buy", "volume");
                        currentTime = System.currentTimeMillis();
                        long timeDiff = currentTime - isLastHourHasLongPositionStartTimeStamp;
                        if (positionVolumeLong > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_SMALL && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE && currentClosingStatusBuy.equals("normal")) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时多头持仓15分钟未清理完成，清仓状态变为 urgent；";
                            currentClosingStatusBuy = "urgent";
                            cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
                        }
                        if (positionVolumeLong > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG && currentClosingStatusBuy.equals("urgent")) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时多头持仓30分钟未清理完成，清仓状态变为 " + "veryUrgent；";
                            currentClosingStatusBuy = "veryUrgent";
                            cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
                        }
                        if (positionVolumeLong > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时多头持仓45分钟未清理完成，清仓状态变为 mostUrgent；";
                            currentClosingStatusBuy = "mostUrgent";
                            cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
                        }
                        if (positionVolumeLong == 0) {
                            isLastHourHasLongPosition = false;
                        }
                    }
                    //如果有上一个小时没有平仓的空头持仓
                    if (isLastHourHasShortPosition) {
                        long positionVolumeShort = (long) queryPosition("sell", "volume");
                        currentTime = System.currentTimeMillis();
                        long timeDiff = currentTime - isLastHourHasShortPositionStartTimeStamp;
                        if (positionVolumeShort > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_SMALL && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE && currentClosingStatusSell.equals("normal")) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时空头持仓15分钟未清理完成，清仓状态变为 urgent；";
                            currentClosingStatusSell = "urgent";
                            cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
                        }
                        if (positionVolumeShort > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG && currentClosingStatusSell.equals("urgent")) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时空头持仓30分钟未清理完成，清仓状态变为 " + "veryUrgent；";
                            currentClosingStatusSell = "veryUrgent";
                            cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
                        }
                        if (positionVolumeShort > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时空头持仓45分钟未清理完成，清仓状态变为 mostUrgent；";
                            currentClosingStatusSell = "mostUrgent";
                            cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
                        }
                        if (positionVolumeShort == 0) {
                            isLastHourHasShortPosition = false;
                        }
                    }
                    if ((!isLastHourHasLongPosition) && (!isLastHourHasShortPosition)) {
                        //解锁之前的forbid状态
                        openForbidLockByLastHourPositionUnclose = false;
                        currentClosingStatusBuy = "normal";
                        currentClosingStatusSell = "normal";
                    }
                    //*********************************************************************************************************************
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
                    //*********************************************************************************************************************
                    //平仓挂单
                    long availablePositionVolume;
                    double cost_hold;
                    double newestPrice;
                    if (currentClosingStatusBuy.equals("normal")) {
                        availablePositionVolume = (long) queryPosition("buy", "available");
                        if (availablePositionVolume > 0) {
                            cost_hold = (double) queryPosition("buy", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 + TAKE_PROFIT_RATE_SMALL);
                            closePositionHangPriceRateMiddle = cost_hold * (1 + TAKE_PROFIT_RATE_MIDDLE);
                            closePositionHangPriceRateBig = cost_hold * (1 + TAKE_PROFIT_RATE_BIG);
                            closePositionHangOrder("buy");
                        }
                    }
                    if (currentClosingStatusBuy.equals("urgent")) {
                        availablePositionVolume = (long) queryPosition("buy", "available");
                        if (availablePositionVolume > 0) {
                            cost_hold = (double) queryPosition("buy", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 + TAKE_PROFIT_RATE_SMALL - URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                            closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                            closePositionHangOrder("buy");
                        }
                    }
                    if (currentClosingStatusBuy.equals("veryUrgent")) {
                        availablePositionVolume = (long) queryPosition("buy", "available");
                        if (availablePositionVolume > 0) {
                            cost_hold = (double) queryPosition("buy", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 + TAKE_PROFIT_RATE_SMALL - VERY_URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                            closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                            closePositionHangOrder("buy");
                        }
                    }
                    if (currentClosingStatusBuy.equals("mostUrgent")) {
                        availablePositionVolume = (long) queryPosition("buy", "available");
                        if (availablePositionVolume > 0) {
                            newestPrice = huobiContractAPI.getTrade("BTC_CQ").getPrice();
                            closePositionHangPriceRateSmall = newestPrice;
                            closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                            closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                            closePositionHangOrder("buy");
                        }
                    }
                    if (currentClosingStatusSell.equals("normal")) {
                        availablePositionVolume = (long) queryPosition("sell", "available");
                        if (availablePositionVolume > 0) {
                            cost_hold = (double) queryPosition("sell", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 - TAKE_PROFIT_RATE_SMALL);
                            closePositionHangPriceRateMiddle = cost_hold * (1 - TAKE_PROFIT_RATE_MIDDLE);
                            closePositionHangPriceRateBig = cost_hold * (1 - TAKE_PROFIT_RATE_BIG);
                            closePositionHangOrder("sell");
                        }
                    }
                    if (currentClosingStatusSell.equals("urgent")) {
                        availablePositionVolume = (long) queryPosition("sell", "available");
                        if (availablePositionVolume > 0) {
                            cost_hold = (double) queryPosition("sell", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 - TAKE_PROFIT_RATE_SMALL + URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                            closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                            closePositionHangOrder("sell");
                        }
                    }
                    if (currentClosingStatusSell.equals("veryUrgent")) {
                        availablePositionVolume = (long) queryPosition("sell", "available");
                        if (availablePositionVolume > 0) {
                            cost_hold = (double) queryPosition("sell", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 - TAKE_PROFIT_RATE_SMALL + VERY_URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                            closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                            closePositionHangOrder("sell");
                        }
                    }
                    if (currentClosingStatusSell.equals("mostUrgent")) {
                        availablePositionVolume = (long) queryPosition("sell", "available");
                        if (availablePositionVolume > 0) {
                            newestPrice = huobiContractAPI.getTrade("BTC_CQ").getPrice();
                            closePositionHangPriceRateSmall = newestPrice;
                            closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                            closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                            closePositionHangOrder("buy");
                        }
                    }
                    //*********************************************************************************************************************
                    //系统损失超过一定幅度，强行平仓
                    profitRateLong = (double) queryPosition("buy", "profit_rate");
                    waitSomeMillis(100);
                    profitRateShort = (double) queryPosition("sell", "profit_rate");
                    //损失超过30%，平仓
                    if (profitRateLong < FORCE_CLOSE_POSITION_LOSS_RATE_MIN) {
                        currentClosingStatusBuy = "mostUrgent";
                        currentOpenningStatus = "forbid";
                        openForbidLockByForceClose = true;
                        cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
                    }
                    if (profitRateShort < FORCE_CLOSE_POSITION_LOSS_RATE_MIN) {
                        currentClosingStatusSell = "mostUrgent";
                        currentOpenningStatus = "forbid";
                        openForbidLockByForceClose = true;
                        cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
                    }

                   //保持所有的状态信息列表的长度
                    fixListLength(openPositionHangStatusList,200);
                    fixListLength(closePositionHangStatusList,200);
                    fixListLength(longShortSwitchStatusList,200);

                    //终止此策略的运行
                    if (!isThisPolicyAvailable) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "策略终止运行";
                        timer.cancel();
                    }

                } catch (IllegalStateException e) {
                    currentPolicyRunningStatus = e.getMessage();
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
            long orderID = huobiContractAPI.placeOrder(contractOrderRequest);
            if (offset.equals("open")) {
                openPositionHangOrderIDList.add(orderID);
                currentPolicyRunningStatus = df.format(new Date()) + " " + offset + " 挂单，价格：" + price + ",数量：" + volume +
                        ", 方向：" + direction;
                openPositionHangStatusList.add(currentPolicyRunningStatus);
            } else {
                closePositionHangOrderIDList.add(orderID);
                currentPolicyRunningStatus = df.format(new Date()) + " " + offset + " 挂单，价格：" + price + ",数量：" + volume +
                        ", 方向：" + direction;
                closePositionHangStatusList.add(currentPolicyRunningStatus);
            }
        }
    }

    //对已有持仓进行平仓挂单,在进入此函数前必须确认availablePositionVolume是大于0
    private void closePositionHangOrder(String direction) {
        // 如果是正常情况，只有在新的持仓出现才会进入此函数，此时就把已有的持仓平单全部撤销
        if (currentClosingStatusBuy.equals("normal")) {
            //撤销平仓挂单列表里的所有订单
            cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
        }
        //重新获得可以平仓挂单的数量
        long availablePositionVolume = (long) queryPosition(direction, "available");
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
        //账户总的权益，在安全比例基础上，可以开的合约总张数
        long maxOpenVolumeBySafePercent = getMaxOpenVolume(totalMargin * POLICYBYLEAD_MARGIN_SAFE_RATE_INIT, "BTC_CQ");
        //可以挂单的数量占之上总张数的百分比
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
