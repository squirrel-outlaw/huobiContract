package com.huobi.api.impl;


import com.huobi.api.HuobiContractAPI;
import com.huobi.api.HuobiContractApiService;
import com.huobi.domain.POJOs.*;
import com.huobi.domain.enums.*;
import com.huobi.domain.response.BatchCancelResp;
import com.huobi.domain.response.RespBody;
import com.huobi.domain.response.RespTick;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;

import static com.huobi.api.HuobiApiServiceGenerator.createService;
import static com.huobi.api.HuobiApiServiceGenerator.executeSync;

/**
 * @Author: squirrel
 * @Date: 18-9-7 下午1:55
 */
public class HuobiContractAPIImpl implements HuobiContractAPI {

    private HuobiContractApiService service;         //用来处理交易的service
    private HuobiContractApiService serviceMarket;  //用来处理行情的service，不加apiKey和apiSecret，查询频率是50ms/次
    private String apiKey;
    private String apiSecret;

    public HuobiContractAPIImpl(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        service = createService(HuobiContractApiService.class, apiKey, apiSecret);
        serviceMarket = createService(HuobiContractApiService.class);
    }

// 行情接口---------------------------------------------------------------------------------

    //获取 Market Depth 数据
    @Override
    public Depth getDepth(String symbol, MergeLevel step) {
        return executeSync(service.getDepth(symbol, step == null ? null : step.getCode())).getTick();
    }

    //获取K线数据
    @Override
    public List<Kline> getKlines(String symbol, Resolution period, String size) {
        List<Kline> klineList = executeSync(serviceMarket.getKlines(symbol, period == null ? null : period.getCode(), size))
                .getData();
        return klineList;
    }

    //获取聚合行情(Ticker)
    @Override
    public Merged getMerged(String symbol) {
        return executeSync(service.getMerged(symbol)).getTick();
    }

    //获取最新成交记录
    @Override
    public Trade getTrade(String symbol) {
        List<Trade> tradeList = executeSync(service.getTrade(symbol)).getTick().getData();  //返回只有一个数据项的tradeList
        return tradeList.get(0);
    }

    //批量获取最近的交易记录
    @Override
    public List<Trade> getHistoryTrades(String symbol, String size) {
        RespBody<List<RespTick<Trade>>> listRespBody = executeSync(service.getHistoryTrades(symbol, size));
        List<Trade> trades = new ArrayList<>();
        for (RespTick<Trade> tick : listRespBody.getData()) {
            trades.addAll(tick.getData());
        }
        return trades;
    }

// 资产接口---------------------------------------------------------------------------------

    //获取用户账户信息
    @Override
    public List<ContractAccountInfo> getContractAccountInfos(String symbol) {
        List<ContractAccountInfo> contractAccountInfoList = executeSync(service.getContractAccountInfos(symbol)).getData();
        return contractAccountInfoList;
    }

    //获取用户持仓信息
    @Override
    public List<ContractPositionInfo> getContractPositionInfos(String symbol) {
        List<ContractPositionInfo> contractPositionInfoList = executeSync(service.getContractPositionInfos(symbol)).getData();
        return contractPositionInfoList;
    }

// 交易接口---------------------------------------------------------------------------------



}

