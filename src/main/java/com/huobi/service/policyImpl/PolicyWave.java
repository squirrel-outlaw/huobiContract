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
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.utils.PrintUtil.print;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-24 下午1:54
 */
@Slf4j
public class PolicyWave {
    private SimpleDateFormat df;   //设置日期格式
    private HuobiContractAPI huobiContractAPI;
    //private String targetPositionStatus;  //目标持仓状态
    private Kline markKline;              //大阳线或大阴线
    private boolean isFindMarkKline = false;
    private String markKlineType;    //大阳线或大阴线，绿色为大阳线，红色为大阴线
    private double openPositionPrice;
    private boolean isOpenPositionPriceChange = false;

    private double marginSafePercent = MARGIN_SAFE_RATE_INIT;  //开仓时的安全比例
    private long marginSafeRateReduceToMaxTimeStamp = 0;
    private long marginSafeRateReduceToMiddleTimeStamp = 0;
    private long marginSafeRateReduceToZeroTimeStamp = 0;

    public boolean isThisPolicyAvailable;
    //以下为显示系统运行状态的信息
    public String currentPolicyRunningStatus;   //系统运行时时的状态信息
    public List<String> forceClosePositionStatusList = new ArrayList<>();   //强行平仓时的信息列表
    public List<String> openClosePositionHangStatusList = new ArrayList<>();  //开平仓挂单时的信息列表
    public List<String> longShortSwitchStatusList = new ArrayList<>();   //系统空多转换时的信息列表


    private String currentPositionStatus;
    private String targetPositionStatus;
    private double markClosePrice;
    private double markHighPrice;


    public PolicyWave(InitSystem initSystem, boolean isThisPolicyAvailable) {
        this.huobiContractAPI = initSystem.huobiContractAPI;
        this.isThisPolicyAvailable = isThisPolicyAvailable;
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public void autoTrade() {
        /*List<Kline> klineList = huobiContractAPI.getKlines("BTC_CQ", Resolution.M60, "200");
        Kline kline1 = new Kline();
        Kline kline2 = new Kline();
        Kline kline3 = new Kline();
        Kline kline4 = new Kline();

        kline1.setLow(3450);
        kline1.setHigh(3630);
        kline1.setOpen(3530);
        kline1.setClose(3620);
        klineList.add(kline1);

        kline2.setLow(3615);
        kline2.setHigh(3640);
        kline2.setOpen(3620);
        kline2.setClose(3630);
        klineList.add(kline2);

        kline3.setLow(3615);
        kline3.setHigh(3640);
        kline3.setOpen(3620);
        kline3.setClose(3634);
        klineList.add(kline3);

        kline4.setLow(3615);
        kline4.setHigh(3640);
        kline4.setOpen(3640);
        kline4.setClose(3615);
        klineList.add(kline4);

*/
        log.info("系统开始运行");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {

                    List<Kline> klineList = huobiContractAPI.getKlines("BTC_CQ", Resolution.M60, String.valueOf(GET_1HOUR_KLINE_COUNTS));
                    double lowestClosePrice = klineList.get(0).getClose();
                    int lowestClosePriceKlineIndex = 0;
                    double highestClosePrice = klineList.get(0).getClose();
                    int highestClosePriceKlineIndex = 0;
                    double klineRateLength = 0;

                    for (int i = 1; i >= klineList.size() - 2; i++) {
                        if (klineList.get(i).getClose() < lowestClosePrice) {
                            lowestClosePrice = klineList.get(i).getClose();
                            lowestClosePriceKlineIndex = i;
                        }
                        if (klineList.get(i).getClose() > highestClosePrice) {
                            highestClosePrice = klineList.get(i).getClose();
                            highestClosePriceKlineIndex = i;
                        }
                        if (lowestClosePriceKlineIndex >= highestClosePriceKlineIndex) {
                            klineRateLength = (lowestClosePriceKlineIndex - highestClosePriceKlineIndex) / highestClosePriceKlineIndex;
                        } else {
                            klineRateLength = (highestClosePriceKlineIndex - lowestClosePriceKlineIndex) / lowestClosePriceKlineIndex;
                        }

                        if (klineRateLength > 1.5) {
                            currentPositionStatus = "long";
                            //targetPositionStatus="short";
                            markHighPrice = klineList.get(highestClosePriceKlineIndex).getHigh();
                            markClosePrice = klineList.get(highestClosePriceKlineIndex).getClose();

                            for (int j = highestClosePriceKlineIndex + 1; j >= klineList.size() - 2; i++) {
                                if (klineList.get(j).getClose() > markClosePrice && currentPositionStatus.equals("long")) {
                                    markClosePrice = klineList.get(j).getClose();
                                }
                                if (klineList.get(j).getClose() < markClosePrice && currentPositionStatus.equals("long")) {
                                    if (j + 1 <= klineList.size() - 2 && klineList.get(j + 1).getClose() < markClosePrice) {
                                        currentPositionStatus = "short";
                                    }
                                }


                            }


                        }
                    }




















                /*    currentPolicyRunningStatus = df.format(new Date()) + " " + "系统正常运行中";
                    //每次循环交易前先初始化isFindMarkKline
                    isFindMarkKline = false;
                    List<Kline> klineList = huobiContractAPI.getKlines("BTC_CQ", Resolution.M60, String.valueOf(GET_1HOUR_KLINE_COUNTS));

                    for (int i = klineList.size() - 2; i >= 0; i--) {
                        //每根K线的实体长度
                        double klineRateLength = (klineList.get(i).getClose() - klineList.get(i).getOpen()) * 100 / klineList
                                .get(i).getOpen();
                        //找到大K线
                        if (klineRateLength > 1.5) {
                            markKline = klineList.get(i);
                            markKlineType = "green";
                            targetPositionStatus = "short";
                            openPositionPrice = klineList.get(i).getClose();
                            isOpenPositionPriceChange = true;
                            isFindMarkKline = true;
                        }
                        if (klineRateLength < -1.5) {
                            markKline = klineList.get(i);
                            markKlineType = "red";
                            targetPositionStatus = "long";
                            openPositionPrice = klineList.get(i).getClose();
                            isOpenPositionPriceChange = true;
                            isFindMarkKline = true;
                        }
                        //如果找到了大K线是出现在上一个小时，全部平仓，直接返回，进行下一次交易循环
                        if (isFindMarkKline && (i == klineList.size() - 2)) {
                            currentPolicyRunningStatus = df.format(new Date()) + " " + "大K线为上一个小时出现的，全部平仓，直接返回，进行下一次交易循环";
                            recordInfoToList(currentPolicyRunningStatus, forceClosePositionStatusList, klineList.get(i).getId());
                            closePositionRightNow("buy", 1);
                            closePositionRightNow("sell", 1);
                            waitSomeSeconds(LATELY_BIG_KLINE_AFTER_FORCE_CLOSE_POSITION_WAIT_TIME);
                            //直接返回，进行下一次交易循环
                            return;
                        }
                        //根据大K线后面的情况分别进行处理
                        if (isFindMarkKline) {
                            for (int j = i + 1; j < klineList.size(); j++) {
                                double nextClosePrice = klineList.get(j).getClose();
                                //大阳线，正常做空状态
                                if (markKlineType.equals("green") && targetPositionStatus.equals("short")) {
                                    //大阳线做空时，最新价下探，调整做空的开仓价
                                    if (nextClosePrice * (1 + OPEN_POSITION_HANG_PRICE_ADJUST_RATE) < openPositionPrice && nextClosePrice * (1 + OPEN_POSITION_HANG_PRICE_ADJUST_RATE) > markKline.getClose() * 0.985) {
                                        openPositionPrice = nextClosePrice * (1 + OPEN_POSITION_HANG_PRICE_ADJUST_RATE);
                                        isOpenPositionPriceChange = true;
                                        if (j == klineList.size() - 1) {
                                            currentPolicyRunningStatus = df.format(new Date()) + " " + "大阳线做空时，最新价下探，调整做空的开仓价";
                                            recordInfoToList(currentPolicyRunningStatus, longShortSwitchStatusList, klineList.get(j).getId());
                                        }
                                        //如果最新的K线突破的大阳线的高点，转为做多
                                    } else if ((nextClosePrice - markKline.getHigh()) / markKline.getHigh() > THROUGH_GREEN_BIG_KLINE_RATE) {
                                        targetPositionStatus = "long";
                                        openPositionPrice = nextClosePrice;
                                        isOpenPositionPriceChange = true;
                                        if (j == klineList.size() - 1) {
                                            currentPolicyRunningStatus = df.format(new Date()) + " " + "最新的K线突破大阳线的高点，转为做多";
                                            recordInfoToList(currentPolicyRunningStatus, longShortSwitchStatusList, klineList.get(j).getId());
                                        }
                                    } else {
                                        isOpenPositionPriceChange = false;
                                    }
                                    continue;
                                }
                                //大阳线，做多状态
                                if (markKlineType.equals("green") && targetPositionStatus.equals("long")) {
                                    //如果最新的K线又回到大阳线的收盘价之下，转为做空
                                    if ((nextClosePrice - markKline.getClose()) / markKline.getClose() < RETURN_GREEN_BIG_KLINE_RATE) {
                                        targetPositionStatus = "short";
                                        openPositionPrice = nextClosePrice;
                                        isOpenPositionPriceChange = true;
                                        if (j == klineList.size() - 1) {
                                            currentPolicyRunningStatus = df.format(new Date()) + " " + "最新的K线又回到大阳线的收盘价之下，转为做空";
                                            recordInfoToList(currentPolicyRunningStatus, longShortSwitchStatusList, klineList.get(j).getId());
                                        }
                                        //大阳线做多时，最新价上扬，时时调整做多开仓价
                                    } else if (nextClosePrice * (1 - OPEN_POSITION_HANG_PRICE_ADJUST_RATE) > openPositionPrice && nextClosePrice * (1 - OPEN_POSITION_HANG_PRICE_ADJUST_RATE) < markKline.getClose() * 1.015) {
                                        openPositionPrice = nextClosePrice * (1 - OPEN_POSITION_HANG_PRICE_ADJUST_RATE);
                                        isOpenPositionPriceChange = true;
                                        if (j == klineList.size() - 1) {
                                            currentPolicyRunningStatus = df.format(new Date()) + " " + "阳线做多时，最新价上扬，时时调整做多开仓价";
                                            recordInfoToList(currentPolicyRunningStatus, longShortSwitchStatusList, klineList.get(j).getId());
                                        }
                                    } else {
                                        isOpenPositionPriceChange = false;
                                    }
                                    continue;
                                }
                                //大阴线，正常做多状态
                                if (markKlineType.equals("red") && targetPositionStatus.equals("long")) {
                                    //大阴线做多时，最新价上扬，时时调整做多开仓价
                                    if (nextClosePrice * (1 - OPEN_POSITION_HANG_PRICE_ADJUST_RATE) > openPositionPrice && nextClosePrice * (1 - OPEN_POSITION_HANG_PRICE_ADJUST_RATE) < markKline.getClose() * 1.015) {
                                        openPositionPrice = nextClosePrice * (1 - OPEN_POSITION_HANG_PRICE_ADJUST_RATE);
                                        isOpenPositionPriceChange = true;
                                        if (j == klineList.size() - 1) {
                                            currentPolicyRunningStatus = df.format(new Date()) + " " + "大阴线做多时，最新价上扬，时时调整做多开仓价";
                                            recordInfoToList(currentPolicyRunningStatus, longShortSwitchStatusList, klineList.get(j).getId());
                                        }
                                        //如果最新的K线突破的大阴线的低点，转为做空
                                    } else if ((nextClosePrice - markKline.getLow()) / markKline.getLow() < THROUGH_RED_BIG_KLINE_RATE) {
                                        targetPositionStatus = "short";
                                        openPositionPrice = nextClosePrice;
                                        isOpenPositionPriceChange = true;
                                        if (j == klineList.size() - 1) {
                                            currentPolicyRunningStatus = df.format(new Date()) + " " + "最新的K线突破大阴线的低点，转为做空";
                                            recordInfoToList(currentPolicyRunningStatus, longShortSwitchStatusList, klineList.get(j).getId());
                                        }
                                    } else {
                                        isOpenPositionPriceChange = false;
                                    }
                                    continue;
                                }
                                //大阴线，做空状态
                                if (markKlineType.equals("red") && targetPositionStatus.equals("short")) {
                                    //如果最新的K线又回到大阴线的收盘价之上，转为做多
                                    if ((nextClosePrice - markKline.getClose()) / markKline.getClose() > RETURN_RED_BIG_KLINE_RATE) {
                                        targetPositionStatus = "long";
                                        openPositionPrice = nextClosePrice;
                                        isOpenPositionPriceChange = true;
                                        if (j == klineList.size() - 1) {
                                            currentPolicyRunningStatus = df.format(new Date()) + " " + "最新的K线又回到大阴线的收盘价之上，转为做多";
                                            recordInfoToList(currentPolicyRunningStatus, longShortSwitchStatusList, klineList.get(j).getId());
                                        }
                                        //大阴线做空时，最新价下探，时时调整做空开仓价
                                    } else if (nextClosePrice * (1 + OPEN_POSITION_HANG_PRICE_ADJUST_RATE) < openPositionPrice && nextClosePrice * (1 + OPEN_POSITION_HANG_PRICE_ADJUST_RATE) > markKline.getClose() * 0.985) {
                                        openPositionPrice = nextClosePrice * (1 + OPEN_POSITION_HANG_PRICE_ADJUST_RATE);
                                        isOpenPositionPriceChange = true;
                                        if (j == klineList.size() - 1) {
                                            currentPolicyRunningStatus = df.format(new Date()) + " " + "阴线做空时，最新价下探，时时调整做空开仓价";
                                            recordInfoToList(currentPolicyRunningStatus, longShortSwitchStatusList, klineList.get(j).getId());
                                        }
                                    } else {
                                        isOpenPositionPriceChange = false;
                                    }
                                }
                            }
                        }

                        //如果已找到大K线，退出外层循环
                        if (isFindMarkKline) {
                            break;
                        }
                    }

                    //如果在整个klineList里没有找到大K线，直接退出交易循环
                    if (!isFindMarkKline) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "没有找到大K线";
                        closePositionRightNow("buy", 1);
                        closePositionRightNow("sell", 1);
                        return;
                    }
//*********************************************************************************************************************
                    //如果可用的保证金大于百分之10,进行开仓挂单
                    if (getAvailableMargin("BTC", true, marginSafePercent) > 0.1) {
                        double availableMargin = getAvailableMargin("BTC", false, marginSafePercent);
                        long maxOpenVolume = getMaxOpenVolume(availableMargin, "BTC_CQ");
                        String direction;
                        double hangPrice;
                        if (targetPositionStatus.equals("short")) {
                            direction = "sell";
                            hangPrice = openPositionPrice * (1 - OPEN_POSITION_HANG_PRICE_SMALL_ADJUST_RATE);
                        } else {
                            direction = "buy";
                            hangPrice = openPositionPrice * (1 + OPEN_POSITION_HANG_PRICE_SMALL_ADJUST_RATE);
                        }
                        ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", hangPrice,
                                maxOpenVolume, direction, "open", 20, "limit");
                        huobiContractAPI.placeOrder(contractOrderRequest);
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "开仓挂单，价格：" + hangPrice + ",数量：" + maxOpenVolume + ",方向：" + direction;
                        openClosePositionHangStatusList.add(currentPolicyRunningStatus);
                    }

                    //如果开仓价格改变了，撤销所有开仓订单
                    if (isOpenPositionPriceChange) {
                        finishCancelAllOrders();
                    }

                    //对已有持仓进行平仓挂单
                    if (targetPositionStatus.equals("long")) {
                        closePositionHangOrder("buy");
                    }
                    if (targetPositionStatus.equals("short")) {
                        closePositionHangOrder("sell");
                    }

                    //如果实际持仓跟目标持仓相反，对已有持仓进行立即平仓挂单
                    if (targetPositionStatus.equals("long")) {
                        closePositionRightNow("sell", 1);
                    }
                    if (targetPositionStatus.equals("short")) {
                        closePositionRightNow("buy", 1);
                    }

                    //系统损失超过一定幅度，强行平仓
                    double profitRateLong = (double) queryPosition("buy", "profit_rate");
                    double profitRateShort = (double) queryPosition("sell", "profit_rate");
                    //损失超过30%，平3成仓位，保证金安全比例降到0.35
                    if (profitRateLong < FORCE_CLOSE_POSITION_LOSS_RATE_MIN || profitRateShort < FORCE_CLOSE_POSITION_LOSS_RATE_MIN) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "系统损失超过30%，强行平仓3成仓位";
                        closePositionAccordingToLoss(FORCE_CLOSE_POSITION_VOLUME_PERCENT_MIN, MARGIN_SAFE_RATE_LOSS_MIN);
                        marginSafeRateReduceToMaxTimeStamp = System.currentTimeMillis();
                   }
                    //损失超过40%，平5成仓位，保证金安全比例降到0.25
                    if (profitRateLong < FORCE_CLOSE_POSITION_LOSS_RATE_MIDDLE || profitRateShort < FORCE_CLOSE_POSITION_LOSS_RATE_MIDDLE) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "系统损失超过40%，强行平仓5成仓位";
                        closePositionAccordingToLoss(FORCE_CLOSE_POSITION_VOLUME_PERCENT_MIDDLE, MARGIN_SAFE_RATE_LOSS_MIDDLE);
                        marginSafeRateReduceToMiddleTimeStamp = System.currentTimeMillis();
                    }
                    //损失超过50%，清仓，保证金安全比例降到0
                    if (profitRateLong < FORCE_CLOSE_POSITION_LOSS_RATE_MAX || profitRateShort < FORCE_CLOSE_POSITION_LOSS_RATE_MAX) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "系统损失超过50%，清仓，保证金安全比例降到0";
                        closePositionAccordingToLoss(FORCE_CLOSE_POSITION_VOLUME_PERCENT_MAX, MARGIN_SAFE_RATE_LOSS_MAX);
                        marginSafeRateReduceToZeroTimeStamp = System.currentTimeMillis();
                    }
                    //根据系统运行的时间还原保证金安全比例
                    if (marginSafeRateReduceToZeroTimeStamp != 0 && System.currentTimeMillis()
                            - marginSafeRateReduceToZeroTimeStamp > MARGIN_SAFE_RATE_SWITCH_TIME) {
                        marginSafeRateReduceToZeroTimeStamp=0;
                        marginSafePercent=MARGIN_SAFE_RATE_LOSS_MIDDLE;
                        marginSafeRateReduceToMiddleTimeStamp=System.currentTimeMillis();
                    }
                    if (marginSafeRateReduceToMiddleTimeStamp != 0 && System.currentTimeMillis()
                            - marginSafeRateReduceToMiddleTimeStamp > MARGIN_SAFE_RATE_SWITCH_TIME) {
                        marginSafeRateReduceToMiddleTimeStamp=0;
                        marginSafePercent=MARGIN_SAFE_RATE_LOSS_MIN;
                        marginSafeRateReduceToMaxTimeStamp=System.currentTimeMillis();
                    }
                    if (marginSafeRateReduceToMaxTimeStamp != 0 && System.currentTimeMillis()
                            - marginSafeRateReduceToMaxTimeStamp > MARGIN_SAFE_RATE_SWITCH_TIME) {
                        marginSafeRateReduceToMaxTimeStamp=0;
                        marginSafePercent=MARGIN_SAFE_RATE_INIT;
                    }

                    //终止此策略的运行
                    if (!isThisPolicyAvailable) {
                        currentPolicyRunningStatus = df.format(new Date()) + " " + "策略终止运行";
                        timer.cancel();
                    }
               */
                } catch (IllegalStateException e) {
                }
            }
        }, 0, AUTO_TRADE_INTERVAL);// 自动交易间隔时间
    }

    //*********************************************************************************************************************
    //*********************************************************************************************************************
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
        double cost_hold = 0;
        double profit_rate = 0;
        List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
        for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
            if (contractPositionInfo.getSymbol().equals("BTC") && contractPositionInfo.getContract_type().equals("quarter") && contractPositionInfo.getDirection().equals(direction)) {
                positionVolume = contractPositionInfo.getVolume();
                available = contractPositionInfo.getAvailable();
                cost_hold = contractPositionInfo.getCost_hold();
                profit_rate = contractPositionInfo.getProfit_rate();
            }
        }
        if (queryItem.equals("volume")) {
            return positionVolume;
        }
        if (queryItem.equals("available")) {
            return available;
        }
        if (queryItem.equals("cost_hold")) {
            return cost_hold;
        }
        if (queryItem.equals("profit_rate")) {
            return profit_rate;
        }
        return "error";
    }

    //对已有持仓进行平仓挂单
    private void closePositionHangOrder(String direction) {
        long availablePosition = (long) queryPosition(direction, "available");
        if (availablePosition > 0) {
            //撤销所有委托订单
            finishCancelAllOrders();
            //重新获得最新的可用的持仓
            availablePosition = (long) queryPosition(direction, "available");
            double cost_hold = (double) queryPosition(direction, "cost_hold");
            double closePositionPrice1;
            double closePositionPrice2;
            String hangDirection;
            if (direction.equals("buy")) {
                closePositionPrice1 = cost_hold * (1 + CLOSE_POSITION_HANG_PRICE_RATE_MAX);
                closePositionPrice2 = cost_hold * (1 + CLOSE_POSITION_HANG_PRICE_RATE_MIDDLE);
                hangDirection = "sell";
            } else {
                closePositionPrice1 = cost_hold * (1 - CLOSE_POSITION_HANG_PRICE_RATE_MAX);
                closePositionPrice2 = cost_hold * (1 - CLOSE_POSITION_HANG_PRICE_RATE_MIDDLE);
                hangDirection = "buy";
            }
            if (availablePosition / 2 >= 5) {
                ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", closePositionPrice1,
                        availablePosition / 2, hangDirection, "close", 20, "limit");
                currentPolicyRunningStatus = df.format(new Date()) + " " + "对已有持仓进行平仓挂单，价格：" + closePositionPrice1 + ",数量：" + availablePosition / 2 + ",方向：" + hangDirection;
                openClosePositionHangStatusList.add(currentPolicyRunningStatus);
                huobiContractAPI.placeOrder(contractOrderRequest);
                contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", closePositionPrice2,
                        availablePosition - availablePosition / 2, hangDirection, "close", 20, "limit");
                currentPolicyRunningStatus = df.format(new Date()) + " " + "对已有持仓进行平仓挂单，价格：" + closePositionPrice2 + ",数量：" + (availablePosition - availablePosition / 2) + ",方向：" + hangDirection;
                openClosePositionHangStatusList.add(currentPolicyRunningStatus);
                huobiContractAPI.placeOrder(contractOrderRequest);
            } else {
                ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null, "", closePositionPrice1,
                        availablePosition, hangDirection, "close", 20, "limit");
                currentPolicyRunningStatus = df.format(new Date()) + " " + "对已有持仓进行平仓挂单，价格：" + closePositionPrice1 + ",数量：" + availablePosition + ",方向：" + hangDirection;
                openClosePositionHangStatusList.add(currentPolicyRunningStatus);
                huobiContractAPI.placeOrder(contractOrderRequest);
            }
        }
    }

    //根据损失的不同，进行平仓处理
    private void closePositionAccordingToLoss(double closePositionVolumePercent, double marginSafePercentAdjust) {
        forceClosePositionStatusList.add(currentPolicyRunningStatus);
        closePositionRightNow("buy", closePositionVolumePercent);
        closePositionRightNow("sell", closePositionVolumePercent);
        marginSafePercent = marginSafePercentAdjust;
        marginSafeRateReduceToMaxTimeStamp = 0;
        marginSafeRateReduceToMiddleTimeStamp = 0;
        marginSafeRateReduceToZeroTimeStamp = 0;
    }

    //立即平仓
    private void closePositionRightNow(String direction, double closePositionPercent) {
        double newestPrice;
        long hangVolume;
        long availableVolume;
        String closeDirection;
        if (direction.equals("sell")) {
            closeDirection = "buy";
        } else {
            closeDirection = "sell";
        }
        long positionVolume = (long) queryPosition(direction, "volume");
        //需要保留下来的仓位
        long needKeepPositionVolume = (long) ((long) queryPosition(direction, "volume") * (1 - closePositionPercent));
        //需要平掉的仓位
        long needClosePositionVolume = positionVolume - needKeepPositionVolume;
        while (needClosePositionVolume > 0) {
            //撤销所有委托订单
            finishCancelAllOrders();
            //重新获取总的持仓量
            positionVolume = (long) queryPosition(direction, "volume");
            needClosePositionVolume = positionVolume - needKeepPositionVolume;
            availableVolume = (long) queryPosition(direction, "available");
            if (availableVolume == positionVolume && needClosePositionVolume
                    > 0) {
                hangVolume = needClosePositionVolume;
            } else {
                continue;
            }
            newestPrice = huobiContractAPI.getTrade("BTC_CQ").getPrice();
            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", null,
                    "", newestPrice, hangVolume, closeDirection, "close", 20, "limit");
            currentPolicyRunningStatus = df.format(new Date()) + " " + "对已有持仓进行立即平仓，价格：" + newestPrice + ",数量：" + hangVolume + ",方向：" + closeDirection;
            forceClosePositionStatusList.add(currentPolicyRunningStatus);
            huobiContractAPI.placeOrder(contractOrderRequest);
            waitSomeSeconds(3);
        }
    }

    //撤销所有订单
    private void finishCancelAllOrders() {
        try {
            huobiContractAPI.cancelAllOrders(new ContractOrderInfoRequest(0, "", "BTC"));
            //等待1秒钟，以便撤单完成
            waitSomeSeconds(1);
        } catch (IllegalStateException E) {
        }
    }

    //让程序暂停运行几秒钟
    private void waitSomeSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception e) {
        }
    }

    //记录一些关键转换到文字列表中，只记录发生时30秒内的
    private void recordInfoToList(String status, List<String> infoList, long klineID) {
        if (System.currentTimeMillis() - klineID * 1000 < RECORD_STATUS_SWITCH_TIME * 1000) {
            infoList.add(status);
        }
    }

}



