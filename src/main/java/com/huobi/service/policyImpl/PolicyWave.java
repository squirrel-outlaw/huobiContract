package com.huobi.service.policyImpl;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.POJOs.ContractOrderInfo;
import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.enums.Resolution;
import com.huobi.service.ActualOrderHandler;
import com.huobi.service.InitSystem;
import lombok.extern.slf4j.Slf4j;

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
    private ActualOrderHandler actualOrderHandler;
    private Kline markKline;
    private boolean isFindMarkKline = false;
    private boolean isThroughMarkKline;

    public PolicyWave(InitSystem initSystem, ActualOrderHandler actualOrderHandler) {
        this.huobiContractAPI = initSystem.huobiContractAPI;
        this.actualOrderHandler = actualOrderHandler;
    }

    public void autoTrade() {
        log.info("系统开始运行");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    List<Kline> klineList = huobiContractAPI.getKlines("BTC_CQ", Resolution.M60, "50");
                    if (!isFindMarkKline) {
                        for (int i = klineList.size() - 2; i >= 2; i--) {
                            double klineRateLength = (klineList.get(i).getClose() - klineList.get(i).getOpen()) * 100 / klineList
                                    .get(i).getOpen();
                            if (klineRateLength > 1.5 || klineRateLength < -1.5) {
                                markKline = klineList.get(i);
                                isFindMarkKline = true;
                                break;
                            }
                        }


                        if (!isFindMarkKline) {
                            long klineIDHigh = 0;
                            long klineIDLow = 0;
                            double highestPrice = klineList.get(0).getClose();
                            double highestPriceMax = klineList.get(0).getClose();
                            double lowestPrice = klineList.get(0).getClose();
                            double lowestPriceMin = klineList.get(0).getClose();
                            for (int i = klineList.size() - 2; i >= 2; i--) {
                                if (klineList.get(i).getClose() > highestPrice) {
                                    highestPrice = klineList.get(i).getClose();
                                    highestPriceMax = klineList.get(i).getHigh();
                                    klineIDHigh = klineList.get(i).getId();
                                }
                                if (klineList.get(i).getClose() < lowestPrice) {
                                    lowestPrice = klineList.get(i).getClose();
                                    lowestPriceMin = klineList.get(i).getLow();
                                    klineIDLow = klineList.get(i).getId();
                                }
                            }
                            if (klineIDHigh > klineIDLow) {
                                markKline.setClose(highestPrice);
                                markKline.setHigh(highestPriceMax);
                                markKline.setOpen(lowestPrice);
                                markKline.setId(klineIDHigh);
                            } else {
                                markKline.setClose(lowestPrice);
                                markKline.setLow(lowestPriceMin);
                                markKline.setOpen(highestPrice);
                                markKline.setId(klineIDLow);
                            }
                            double klineRateLength = (markKline.getClose() - markKline.getOpen()) * 100 / markKline
                                    .getOpen();
                            if (klineRateLength > 1.5 || klineRateLength < -1.5) {
                                isFindMarkKline = true;
                            } else {
                                isFindMarkKline = false;
                                markKline = null;
                            }
                        }

                        if (actualPositionStatus.equals("empty") && isFindMarkKline) {
                            if (markKline.getId() <= klineList.get(klineList.size() - 3).getId()) {
                                //标志线是大阳线
                                if (markKline.getClose() > markKline.getOpen()) {
                                    //开空
                                } else {
                                    //开多
                                }

                            }

                        }
                        if (!actualPositionStatus.equals("empty")) {
                            if ((markKline.getClose() > markKline.getOpen()) && (klineList.get(klineList.size() - 1)
                                    .getClose() > markKline.getClose())) {
                                //平空
                            }
                            if ((markKline.getClose() < markKline.getOpen()) && (klineList.get(klineList.size() - 1)
                                    .getClose() < markKline.getClose())) {
                                //平多
                            }




                        }


                    }

                } catch (IllegalStateException e) {
                }
            }
        }, 0, HANG_ORDER_INTERVAL);// 取消订单，重新挂单的间隔为2s
    }
}
