package com.huobi.service.policyImpl;

import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.enums.*;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.InitSystem;
import com.huobi.service.Policy;

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
    private KlineStatus klineStatus = KlineStatus.green;
    private OpenPositionStatus openPositionStatus = OpenPositionStatus.normal;
    private boolean openForbidLockByLastHourPositionUnclose = false;
    private boolean openLimitLockByForceClose = false;
    private int forceClosePositionCount = 0;   //达到强行平仓条件的计数
    private long forceClosePositionStartTimeStamp;

    private ClosePositionStatus closePositionStatusLong = ClosePositionStatus.normal;
    private ClosePositionStatus closePositionStatusShort = ClosePositionStatus.normal;


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

                    List<Kline> klineDayList = huobiContractAPI.getKlines("BTC_CQ", Resolution.D1, "1");
                    double todayKlineLengthRate = (klineDayList.get(0).getClose() - klineDayList.get(0).getOpen()) * 100 / klineDayList.get(0).getOpen();
                    List<Kline> klineList = huobiContractAPI.getKlines("BTC_CQ", Resolution.M60, "3");
                    double lastKlineLengthRate = (klineList.get(1).getClose() - klineList.get(1).getOpen()) * 100 / klineList.get(1).getOpen();
                    double secondLastKlineLengthRate = (klineList.get(0).getClose() - klineList.get(0).getOpen()) * 100 / klineList.get(0).getOpen();

                    if (todayKlineLengthRate > TODAY_KLINE_LENGTH_RATE_UP_LIMIT && !openLimitLockByForceClose) {
                        if (openPositionStatus != OpenPositionStatus.todayKlineBigGreen) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "当天涨幅过大，调高开仓挂单幅度";
                            openPositionStatus = OpenPositionStatus.todayKlineBigGreen;
                            cancelOrdersAccordingOrderIdList(openPositionHangOrderIDList);
                        }
                    } else if (todayKlineLengthRate < TODAY_KLINE_LENGTH_RATE_DOWN_LIMIT && !openLimitLockByForceClose) {
                        if (openPositionStatus != OpenPositionStatus.todayKlineBigRed) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "当天跌幅过大，调高开仓挂单幅度";
                            openPositionStatus = OpenPositionStatus.todayKlineBigRed;
                            cancelOrdersAccordingOrderIdList(openPositionHangOrderIDList);
                        }
                    } else if (lastKlineLengthRate > 1.5 && !openLimitLockByForceClose) {
                        if (openPositionStatus != OpenPositionStatus.riseOpenShort) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上一个小时的K线是大阳线,调高开空幅度";
                            openPositionStatus = OpenPositionStatus.riseOpenShort;
                            cancelOrdersAccordingOrderIdList(openPositionHangOrderIDList);
                        }
                    } else if (lastKlineLengthRate < -1.5 && !openLimitLockByForceClose) {
                        if (openPositionStatus != OpenPositionStatus.reduceOpenLong) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上一个小时的K线是大阴线,降低开多幅度";
                            openPositionStatus = OpenPositionStatus.reduceOpenLong;
                            cancelOrdersAccordingOrderIdList(openPositionHangOrderIDList);
                        }
                    } else if (secondLastKlineLengthRate > 1 && !openForbidLockByLastHourPositionUnclose && !openLimitLockByForceClose) {
                        if (openPositionStatus != OpenPositionStatus.carefulShort) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上上一个小时的K线是大阳线,调高开空幅度";
                            openPositionStatus = OpenPositionStatus.carefulShort;
                            cancelOrdersAccordingOrderIdList(openPositionHangOrderIDList);
                        }
                    } else if (secondLastKlineLengthRate < -1 && !openForbidLockByLastHourPositionUnclose && !openLimitLockByForceClose) {
                        if (openPositionStatus != OpenPositionStatus.carefulLong) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上上一个小时的K线是大阴线,降低开多幅度";
                            openPositionStatus = OpenPositionStatus.carefulLong;
                            cancelOrdersAccordingOrderIdList(openPositionHangOrderIDList);
                        }
                    } else if (!openForbidLockByLastHourPositionUnclose && !openLimitLockByForceClose) {
                        if (openPositionStatus != OpenPositionStatus.normal) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "正常开仓状态";
                            openPositionStatus = OpenPositionStatus.normal;
                            cancelOrdersAccordingOrderIdList(openPositionHangOrderIDList);
                        }
                    }
                    //*********************************************************************************************************************
                    //整点时进行一次初始化过程
                    long currentTime = System.currentTimeMillis();
                    long currentKlineStartTime = klineList.get(2).getId() * 1000;
                    if (currentTime < currentKlineStartTime + WHOLE_TIME_STATUS_SWITCH_CONTINUED) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "最新1小时K线刚刚开始20秒，清除所有订单；";
                        //清除所有订单，做一次初始化的过程
                        finishCancelAllOrders();
                        lastHourHasLongPositionVolume = (long) queryPosition("buy", "volume");
                        lastHourHasShortPositionVolume = (long) queryPosition("sell", "volume");
                        if (lastHourHasLongPositionVolume > 0) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "最新1小时K线刚刚开始20秒，发现上一个小时还有多头持仓，清理此持仓";
                            //给下面的forbid状态上锁
                            openForbidLockByLastHourPositionUnclose = true;
                            openPositionStatus = OpenPositionStatus.forbid;
                            isLastHourHasLongPosition = true;
                            isLastHourHasLongPositionStartTimeStamp = currentTime;
                        }
                        if (lastHourHasShortPositionVolume > 0) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "最新1小时K线刚刚开始15秒，发现上一个小时还有空头持仓，清理此持仓";
                            //给下面的forbid状态上锁
                            openForbidLockByLastHourPositionUnclose = true;
                            openPositionStatus = OpenPositionStatus.forbid;
                            isLastHourHasShortPosition = true;
                            isLastHourHasShortPositionStartTimeStamp = currentTime;
                        }
                    }
                    //如果有上一个小时没有平仓的多头持仓
                    if (isLastHourHasLongPosition) {
                        long positionVolumeLong = (long) queryPosition("buy", "volume");
                        long timeDiff = System.currentTimeMillis() - isLastHourHasLongPositionStartTimeStamp;
                        if (positionVolumeLong > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_SMALL && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE
                                && closePositionStatusLong == ClosePositionStatus.normal) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时多头持仓15分钟未清理完成，清仓状态变为 urgent；";
                            closePositionStatusLong = ClosePositionStatus.urgent;
                        }
                        if (positionVolumeLong > 0 && timeDiff >
                                LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG
                                && closePositionStatusLong == ClosePositionStatus.urgent) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时多头持仓30分钟未清理完成，清仓状态变为 " + "veryUrgent；";
                            closePositionStatusLong = ClosePositionStatus.veryUrgent;
                        }
                        if (positionVolumeLong > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时多头持仓45分钟未清理完成，清仓状态变为 mostUrgent；";
                            closePositionStatusLong = ClosePositionStatus.mostUrgent;
                        }
                        if (positionVolumeLong == 0) {
                            isLastHourHasLongPosition = false;
                        }
                    }
                    //如果有上一个小时没有平仓的空头持仓
                    if (isLastHourHasShortPosition) {
                        long positionVolumeShort = (long) queryPosition("sell", "volume");
                        long timeDiff = System.currentTimeMillis() - isLastHourHasShortPositionStartTimeStamp;
                        if (positionVolumeShort > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_SMALL && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE
                                && closePositionStatusShort == ClosePositionStatus.normal) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时空头持仓15分钟未清理完成，清仓状态变为 urgent；";
                            closePositionStatusShort = ClosePositionStatus.urgent;
                        }
                        if (positionVolumeShort > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE && timeDiff <= LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG
                                && closePositionStatusShort == ClosePositionStatus.urgent) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时空头持仓30分钟未清理完成，清仓状态变为 " + "veryUrgent；";
                            closePositionStatusShort = ClosePositionStatus.veryUrgent;
                        }
                        if (positionVolumeShort > 0 && timeDiff > LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "上1小时空头持仓45分钟未清理完成，清仓状态变为 mostUrgent；";
                            closePositionStatusShort = ClosePositionStatus.mostUrgent;
                        }
                        if (positionVolumeShort == 0) {
                            isLastHourHasShortPosition = false;
                        }
                    }
                    if ((!isLastHourHasLongPosition) && (!isLastHourHasShortPosition)) {
                        //解锁之前的forbid状态
                        openForbidLockByLastHourPositionUnclose = false;
                        closePositionStatusLong = ClosePositionStatus.normal;
                        closePositionStatusShort = ClosePositionStatus.normal;
                    }
                    //*********************************************************************************************************************
                    //最新K线红绿转换时清除开仓订单
                    double newestKlineLengthRate = (klineList.get(2).getClose() - klineList.get(2).getOpen()) * 100 / klineList.get(2).getOpen();
                    if (newestKlineLengthRate > 0) {
                        if (klineStatus != KlineStatus.green) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "当前K线由红转绿，清除开仓订单；";
                            cancelOrdersAccordingOrderIdList(openPositionHangOrderIDList);
                        }
                        klineStatus = KlineStatus.green;
                    }
                    if (newestKlineLengthRate < 0) {
                        if (klineStatus != KlineStatus.red) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "当前K线由绿转红，清除开仓订单；";
                            cancelOrdersAccordingOrderIdList(openPositionHangOrderIDList);
                        }
                        klineStatus = KlineStatus.red;
                    }
                    //如果可用的保证金比例大于0.05,并且currentOpenningStatus不为forbid，才进行开仓挂单
                    if (getAvailableMargin("BTC", true, marginSafePercent) > 0.05 && openPositionStatus != OpenPositionStatus.forbid) {
                        if (newestKlineLengthRate > 0) {
                            if (openPositionStatus == OpenPositionStatus.todayKlineBigGreen) {
                                openPositionHangPriceRateSmall = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_SMALL + OPEN_POSITION_RATE_TODAY_BIG_KLINE_ADJUST);
                                openPositionHangPriceRateMiddle = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_MIDDLE + OPEN_POSITION_RATE_TODAY_BIG_KLINE_ADJUST);
                                openPositionHangPriceRateBig = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_BIG + OPEN_POSITION_RATE_TODAY_BIG_KLINE_ADJUST);
                            } else if (openPositionStatus == OpenPositionStatus.riseOpenShort) {
                                openPositionHangPriceRateSmall = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_SMALL + OPEN_POSITION_RATE_LAST_HOUR_BIG_KLINE_ADJUST);
                                openPositionHangPriceRateMiddle = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_MIDDLE + OPEN_POSITION_RATE_LAST_HOUR_BIG_KLINE_ADJUST);
                                openPositionHangPriceRateBig = klineList.get(2).getOpen() * (1 + OPEN_POSITION_RATE_BIG + OPEN_POSITION_RATE_LAST_HOUR_BIG_KLINE_ADJUST);
                            } else if (openPositionStatus == OpenPositionStatus.carefulShort) {
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
                            if (openPositionStatus == OpenPositionStatus.carefulLong) {
                                openPositionHangPriceRateSmall = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_SMALL - OPEN_POSITION_RATE_CAREFUL_SHORT_ADJUST);
                                openPositionHangPriceRateMiddle = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_MIDDLE - OPEN_POSITION_RATE_CAREFUL_SHORT_ADJUST);
                                openPositionHangPriceRateBig = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_BIG - OPEN_POSITION_RATE_CAREFUL_SHORT_ADJUST);
                            } else {
                                openPositionHangPriceRateSmall = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_SMALL);
                                openPositionHangPriceRateMiddle = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_MIDDLE);
                                openPositionHangPriceRateBig = klineList.get(2).getOpen() * (1 - OPEN_POSITION_RATE_BIG);
                            }
                        }
                        openPositionHangOrder();
                    }
                    //*********************************************************************************************************************
                    //平仓挂单
                    long availablePositionVolume;
                    long totalPositionVolume;
                    double cost_hold;
                    double newestPrice;
                    //*************平多的情况*************
                    if (closePositionStatusLong == ClosePositionStatus.normal) {
                        availablePositionVolume = (long) queryPosition("buy", "available");
                        if (availablePositionVolume > 0) {
                            cost_hold = (double) queryPosition("buy", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 + TAKE_PROFIT_RATE_SMALL);
                            closePositionHangPriceRateMiddle = cost_hold * (1 + TAKE_PROFIT_RATE_MIDDLE);
                            closePositionHangPriceRateBig = cost_hold * (1 + TAKE_PROFIT_RATE_BIG);
                            //在下面函数中一开始会清除所有平仓订单
                            closePositionHangOrder("buy");
                        }
                    }
                    if (closePositionStatusLong == ClosePositionStatus.urgent) {
                        totalPositionVolume = (long) queryPosition("buy", "volume");
                        if (totalPositionVolume > 0) {
                            cost_hold = (double) queryPosition("buy", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 + TAKE_PROFIT_RATE_SMALL - URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateMiddle = cost_hold * (1 + TAKE_PROFIT_RATE_MIDDLE - URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateBig = closePositionHangPriceRateMiddle;
                            //在下面函数中一开始会清除所有平仓订单
                            closePositionHangOrder("buy");
                        }
                    }
                    if (closePositionStatusLong == ClosePositionStatus.veryUrgent) {
                        totalPositionVolume = (long) queryPosition("buy", "volume");
                        if (totalPositionVolume > 0) {
                            cost_hold = (double) queryPosition("buy", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 + TAKE_PROFIT_RATE_SMALL - VERY_URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateMiddle = cost_hold * (1 + TAKE_PROFIT_RATE_MIDDLE - VERY_URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateBig = closePositionHangPriceRateMiddle;
                            //在下面函数中一开始会清除所有平仓订单
                            closePositionHangOrder("buy");
                        }
                    }
                    if (closePositionStatusLong == ClosePositionStatus.mostUrgent || closePositionStatusLong == ClosePositionStatus.rightNow) {
                        totalPositionVolume = (long) queryPosition("buy", "volume");
                        if (totalPositionVolume > 0) {
                            if (closePositionStatusLong == ClosePositionStatus.mostUrgent) {
                                double lowestSellPrice = huobiContractAPI.getDepth("BTC_CQ", MergeLevel.STEP0).getAsks().get(0).get(0);
                                newestPrice = lowestSellPrice - 0.1;
                            } else {
                                newestPrice = huobiContractAPI.getTrade("BTC_CQ").getPrice();
                            }
                            closePositionHangPriceRateSmall = newestPrice;
                            closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                            closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                            //在下面函数中一开始会清除所有平仓订单
                            closePositionHangOrder("buy");
                        }
                    }
                    //*************平空的情况*************
                    if (closePositionStatusShort == ClosePositionStatus.normal) {
                        availablePositionVolume = (long) queryPosition("sell", "available");
                        if (availablePositionVolume > 0) {
                            cost_hold = (double) queryPosition("sell", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 - TAKE_PROFIT_RATE_SMALL);
                            closePositionHangPriceRateMiddle = cost_hold * (1 - TAKE_PROFIT_RATE_MIDDLE);
                            closePositionHangPriceRateBig = cost_hold * (1 - TAKE_PROFIT_RATE_BIG);
                            //在下面函数中一开始会清除所有平仓订单
                            closePositionHangOrder("sell");
                        }
                    }
                    if (closePositionStatusShort == ClosePositionStatus.urgent) {
                        totalPositionVolume = (long) queryPosition("sell", "volume");
                        if (totalPositionVolume > 0) {
                            cost_hold = (double) queryPosition("sell", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 - TAKE_PROFIT_RATE_SMALL + URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateMiddle = cost_hold * (1 - TAKE_PROFIT_RATE_MIDDLE + URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateBig = closePositionHangPriceRateMiddle;
                            //在下面函数中一开始会清除所有平仓订单
                            closePositionHangOrder("sell");
                        }
                    }
                    if (closePositionStatusShort == ClosePositionStatus.veryUrgent) {
                        totalPositionVolume = (long) queryPosition("sell", "volume");
                        if (totalPositionVolume > 0) {
                            cost_hold = (double) queryPosition("sell", "cost_hold");
                            closePositionHangPriceRateSmall = cost_hold * (1 - TAKE_PROFIT_RATE_SMALL + VERY_URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateMiddle = cost_hold * (1 - TAKE_PROFIT_RATE_MIDDLE + VERY_URGENT_CLOSE_POSITION_HANG_RATE_ADJUST);
                            closePositionHangPriceRateBig = closePositionHangPriceRateMiddle;
                            //在下面函数中一开始会清除所有平仓订单
                            closePositionHangOrder("sell");
                        }
                    }
                    if (closePositionStatusShort == ClosePositionStatus.mostUrgent || closePositionStatusShort == ClosePositionStatus.rightNow) {
                        totalPositionVolume = (long) queryPosition("sell", "volume");
                        if (totalPositionVolume > 0) {
                            if (closePositionStatusShort == ClosePositionStatus.mostUrgent) {
                                double highestBuyPrice = huobiContractAPI.getDepth("BTC_CQ", MergeLevel.STEP0).getBids().get(0).get(0);
                                newestPrice = highestBuyPrice + 0.1;
                            } else {
                                newestPrice = huobiContractAPI.getTrade("BTC_CQ").getPrice();
                            }
                            closePositionHangPriceRateSmall = newestPrice;
                            closePositionHangPriceRateMiddle = closePositionHangPriceRateSmall;
                            closePositionHangPriceRateBig = closePositionHangPriceRateSmall;
                            //在下面函数中一开始会清除所有平仓订单
                            closePositionHangOrder("sell");
                        }
                    }
                    //*********************************************************************************************************************
                    //系统损失超过一定幅度，强行平仓
                    profitRateLong = (double) queryPosition("buy", "profit_rate");
                    waitSomeMillis(100);
                    profitRateShort = (double) queryPosition("sell", "profit_rate");
                    //损失超过25%，平仓
                    if (profitRateLong < FORCE_CLOSE_POSITION_LOSS_RATE) {
                        forceClosePositionCount = forceClosePositionCount + 1;
                        if (forceClosePositionCount == FORCE_CLOSE_POSITION_COUNT_MAX) {
                            forceClosePositionStartTimeStamp = System.currentTimeMillis();
                            openLimitLockByForceClose = true;
                            openPositionStatus = OpenPositionStatus.afterForceClosePosition;
                            closePositionStatusLong = ClosePositionStatus.rightNow;
                        }
                    } else if (profitRateShort < FORCE_CLOSE_POSITION_LOSS_RATE) {
                        forceClosePositionCount = forceClosePositionCount + 1;
                        if (forceClosePositionCount == FORCE_CLOSE_POSITION_COUNT_MAX) {
                            forceClosePositionStartTimeStamp = System.currentTimeMillis();
                            openLimitLockByForceClose = true;
                            openPositionStatus = OpenPositionStatus.afterForceClosePosition;
                            closePositionStatusShort = ClosePositionStatus.rightNow;
                        }
                    } else {
                        forceClosePositionCount = 0;
                        closePositionStatusLong = ClosePositionStatus.normal;
                        closePositionStatusShort = ClosePositionStatus.normal;
                        //强行平仓后，开仓限制持续时间1小时
                        if (openPositionStatus == OpenPositionStatus.afterForceClosePosition && System.currentTimeMillis() - forceClosePositionStartTimeStamp > OPEN_LIMIT_LOCK_DURATION_TIME) {
                            openLimitLockByForceClose = false;
                        }
                    }

                    //保持所有的状态信息列表的长度
                    fixListLength(openPositionHangInfoList, 200);
                    fixListLength(closePositionHangInfoList, 200);
                    fixListLength(longShortSwitchInfoList, 200);

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
    private void openClosePositionHangBasic(long volume, double price, String direction, String offset) {
        if (volume > 0) {
            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", price,
                    volume, direction, offset, 20, "limit");
            long orderID = huobiContractAPI.placeOrder(contractOrderRequest);
            if (offset.equals("open")) {
                openPositionHangOrderIDList.add(orderID);
                currentPolicyRunningStatus = df.format(new Date()) + " " + offset + " 挂单，价格：" + price + ",数量：" + volume + ", 方向：" + direction;
                openPositionHangInfoList.add(currentPolicyRunningStatus);
            } else {
                closePositionHangOrderIDList.add(orderID);
                currentPolicyRunningStatus = df.format(new Date()) + " " + offset + " 挂单，价格：" + price + ",数量：" + volume + ", 方向：" + direction;
                closePositionHangInfoList.add(currentPolicyRunningStatus);
            }
        }
    }

    //对可用的保证金进行开仓挂单
    private void openPositionHangOrder() {
        double availableMargin = getAvailableMargin("BTC", false, marginSafePercent);
        long maxOpenVolume = getMaxOpenVolume(availableMargin, "BTC_CQ");
        String hangDirection;
        long hangVolumeRateSmall;
        long hangVolumeRateMiddle = 0;
        long hangVolumeRateBig = 0;
        if (klineStatus == KlineStatus.green) {
            hangDirection = "sell";
        } else {
            hangDirection = "buy";
        }
        double availableMarginPercent = getAvailableMargin("BTC", true, marginSafePercent);
        if (availableMarginPercent > 0.7) {
            hangVolumeRateSmall = (long) (maxOpenVolume * 0.35) + 1;
            hangVolumeRateMiddle = (long) (maxOpenVolume * 0.35) + 1;
            hangVolumeRateBig = maxOpenVolume - hangVolumeRateSmall - hangVolumeRateMiddle;
        } else if (availableMarginPercent > 0.33) {
            hangVolumeRateSmall = (long) (maxOpenVolume * 0.5);
            hangVolumeRateMiddle = maxOpenVolume - hangVolumeRateSmall;
        } else {
            hangVolumeRateSmall = maxOpenVolume;
        }
        openClosePositionHangBasic(hangVolumeRateSmall, openPositionHangPriceRateSmall, hangDirection, "open");
        openClosePositionHangBasic(hangVolumeRateMiddle, openPositionHangPriceRateMiddle, hangDirection, "open");
        openClosePositionHangBasic(hangVolumeRateBig, openPositionHangPriceRateBig, hangDirection, "open");
    }

    //对已有持仓进行平仓挂单,在进入此函数前必须确认availablePositionVolume是大于0
    private void closePositionHangOrder(String direction) {
        //撤销平仓挂单列表里的所有订单
        cancelOrdersAccordingOrderIdList(closePositionHangOrderIDList);
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
        //可以挂单的数量占之上总张数的百分比,先把分子转换成double，不然得出的结果是0
        double availablePositionVolumePercent = (double) availablePositionVolume / maxOpenVolumeBySafePercent;
        if (availablePositionVolumePercent > 0.7) {
            hangVolumeRateSmall = (long) (availablePositionVolume * 0.35) + 1;
            hangVolumeRateMiddle = (long) (availablePositionVolume * 0.35) + 1;
            hangVolumeRateBig = availablePositionVolume - hangVolumeRateSmall - hangVolumeRateMiddle;
        } else if (availablePositionVolumePercent > 0.33) {
            hangVolumeRateSmall = (long) (availablePositionVolume * 0.5) + 1;
            hangVolumeRateMiddle = availablePositionVolume - hangVolumeRateSmall;
        } else {
            hangVolumeRateSmall = availablePositionVolume;
        }
        openClosePositionHangBasic(hangVolumeRateSmall, closePositionHangPriceRateSmall, hangDirection, "close");
        openClosePositionHangBasic(hangVolumeRateMiddle, closePositionHangPriceRateMiddle, hangDirection, "close");
        openClosePositionHangBasic(hangVolumeRateBig, closePositionHangPriceRateBig, hangDirection, "close");
    }

}
