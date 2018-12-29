package com.huobi.service;

import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.POJOs.Symbol;
import com.huobi.domain.POJOs.Trade;
import com.huobi.domain.enums.Resolution;

import java.util.*;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.utils.DateUtil.getHourPointTimestamp;
import static com.huobi.utils.ListUtil.fixListLength;
import static com.huobi.utils.ListUtil.listSampleHandle;
import static com.huobi.utils.PrintUtil.print;

/**
 * @Description 用于从官方获取各种数据，并进行数据的规整
 * @Author squirrel
 * @Date 18-9-19 下午3:03
 */
public class DataManager {
    public InitSystem initSystem;

    public List<Double> BTCPriceRateList = new ArrayList<>();  //BTC实时价格涨跌幅列表，结果为涨跌幅*100
    public double BTCTodayOpenPrice;  //BTC当天的开盘价格
    public List<Double> ETHPriceRateList = new ArrayList<>();  //ETH实时价格涨跌幅列表，结果为涨跌幅*100
    public double ETHTodayOpenPrice;  //ETH当天的开盘价格


    public DataManager(InitSystem initSystem) {
        this.initSystem = initSystem;

    }


    /**
     * @Description: 定时更新合约实时价格涨跌幅
     * @param: interval：更新的间隔（单位为秒; samplingCounts：采样的个数; symbol：合约名称
     * @return:
     */
    public void timingUpdateRealTimePriceRate(double interval, int samplingCounts, String symbol) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                double priceTotal = 0d;
                for (int i = 0; i < samplingCounts; i++) {
                    Trade newestTrade = initSystem.huobiContractAPI.getTrade(symbol);
                    // 如果获得的最近交易记录的时间戳，大于系统当前存储的明天0点的时间戳10秒，
                    // 则更新initSystem对象里存储的明天0点的时间戳,并更新所有交易对当天的开盘价格
                    if (newestTrade.getTs() > initSystem.tomorrowZeroHourTimestamp + 10 * 1000) {
                        initSystem.tomorrowZeroHourTimestamp = getHourPointTimestamp(24);
                        updateAllSymbolsTodayOpenPrice();
                        return;
                    }
                    priceTotal = priceTotal + btcusdtNewestTrade.getPrice();
                }
                double priceAverage = priceTotal / samplingCounts;
                BTCPriceRateList.add((priceAverage - BTCTodayOpenPrice) * 100
                        / BTCTodayOpenPrice);
                BTCPriceRateList = fixListLength(BTCPriceRateList, REALTIME_PRICE_LIST_FIXED_LENGTH);
            }
        }, 0, (int) (interval * 1000));
    }


}



