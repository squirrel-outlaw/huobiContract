package com.huobi.service;

import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.POJOs.Trade;
import com.huobi.domain.enums.Resolution;

import java.util.*;

import static com.huobi.constant.HuobiConsts.ALL_CONTRACT_SYMBOLS;
import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.utils.DateUtil.getHourPointTimestamp;
import static com.huobi.utils.ListUtil.fixListLength;
import static com.huobi.utils.PrintUtil.print;

/**
 * @Description 用于从官方获取各种数据，并进行数据的规整
 * @Author squirrel
 * @Date 18-9-19 下午3:03
 */
public class DataManager {
    public InitSystem initSystem;

    public Map<String, Double> allSymbolsTodayOpenPriceMap = new HashMap<>();  //所有合约交易对当天的开盘价
    public List<Double> BTCPriceRateList;  //ETH实时价格涨跌幅列表，结果为涨跌幅*100


    public DataManager(InitSystem initSystem) {
        this.initSystem = initSystem;
        //初始化所有交易对当天开盘价格
        updateAllSymbolsTodayOpenPrice();
        BTCPriceRateList = timingUpdateRealTimePriceRate(SAMPLING_INTERVAL, SAMPLING_COUNTS, "BTC_CQ");
    }


    /**
     * @Description: 定时更新合约实时价格涨跌幅
     * @param: interval：更新的间隔（单位为秒; samplingCounts：采样的个数; symbol：合约名称
     * @return:
     */
    public List<Double> timingUpdateRealTimePriceRate(double interval, int samplingCounts, String symbol) {
        List<Double> priceRateList = new ArrayList<>();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    double priceTotal = 0d;
                    for (int i = 0; i < samplingCounts; i++) {
                        Trade newestTrade = initSystem.huobiContractAPI.getTrade(symbol);
                        // 如果获得的最近交易记录的时间戳，大于系统当前存储的明天0点的时间戳10秒，
                        // 则更新initSystem对象里存储的明天0点的时间戳,并更新所有交易对当天的开盘价格
                        if (newestTrade.getTs() > initSystem.tomorrowZeroHourTimestamp + 10 * 1000) {
                            initSystem.tomorrowZeroHourTimestamp = getHourPointTimestamp(24);
                            //updateAllSymbolsTodayOpenPrice();
                            return;
                        }
                        priceTotal = priceTotal + newestTrade.getPrice();
                    }
                    double priceAverage = priceTotal / samplingCounts;
                    priceRateList.add((priceAverage - allSymbolsTodayOpenPriceMap.get(symbol)) * 100
                            / allSymbolsTodayOpenPriceMap.get(symbol));
                    fixListLength(priceRateList, REALTIME_PRICE_LIST_FIXED_LENGTH);
                } catch (IllegalStateException e) {
                    //如果出现服务器连接故障，清空priceRateList
                    priceRateList.clear();
                }
            }
        }, 0, (int) (interval * 1000));
        return priceRateList;
    }

    //更新所有合约交易对当天的开盘价格
    private void updateAllSymbolsTodayOpenPrice() {
        for (String symbol : ALL_CONTRACT_SYMBOLS) {
            try {
                Thread.sleep(REQUEST_INTERVAL_LONG);
            } catch (Exception e) {
            }
            try {
                Kline oneDayKline = initSystem.huobiContractAPI.getKlines(symbol, Resolution.D1, "1").get(0);
                allSymbolsTodayOpenPriceMap.put(symbol, oneDayKline.getOpen());
            } catch (IllegalStateException e) {
            }
        }

    }
}



