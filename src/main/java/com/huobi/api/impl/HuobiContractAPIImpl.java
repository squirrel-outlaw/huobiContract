package com.huobi.api.impl;


import com.huobi.api.HuobiContractAPI;
import com.huobi.api.HuobiContractApiService;
import com.huobi.domain.POJOs.*;
import com.huobi.domain.enums.*;
import com.huobi.domain.request.ContractOrderInfoRequest;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.domain.response.CancelOrderResp;
import com.huobi.domain.response.RespBody;
import com.huobi.domain.response.RespTick;

import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.List;

import static com.huobi.api.HuobiApiServiceGenerator.createService;
import static com.huobi.api.HuobiApiServiceGenerator.executeSync;

/**
 * @Author: squirrel
 * @Date: 18-9-7 下午1:55
 */
public class HuobiContractAPIImpl implements HuobiContractAPI {

    private HuobiContractApiService service;         //用来处理交易的service
    private HuobiContractApiService serviceMarket;  //用来处理行情的service，不加apiKey和apiSecret，查询频率是50ms/次

    public HuobiContractAPIImpl(String apiKey, String apiSecret) {
        service = createService(HuobiContractApiService.class, apiKey, apiSecret);
        serviceMarket = createService(HuobiContractApiService.class);
    }

    // 行情接口---------------------------------------------------------------------------------
    //获取 Market Depth 数据
    @Override
    public Depth getDepth(String symbol, MergeLevel step) {
        return executeSync(serviceMarket.getDepth(symbol, step == null ? null : step.getCode())).getTick();
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
        return executeSync(serviceMarket.getMerged(symbol)).getTick();
    }

    //获取最新成交记录
    @Override
    public Trade getTrade(String symbol) {
        List<Trade> tradeList = executeSync(serviceMarket.getTrade(symbol)).getTick().getData();  //返回只有一个数据项的tradeList
        return tradeList.get(0);
    }

    //批量获取最近的交易记录
    @Override
    public List<Trade> getHistoryTrades(String symbol, String size) {
        RespBody<List<RespTick<Trade>>> listRespBody = executeSync(serviceMarket.getHistoryTrades(symbol, size));
        List<Trade> trades = new ArrayList<>();
        for (RespTick<Trade> tick : listRespBody.getData()) {
            trades.addAll(tick.getData());
        }
        return trades;
    }

    // 资产接口---------------------------------------------------------------------------------
    //获取用户账户信息
    @Override
    public List<ContractAccountInfo> getContractAccountInfos() {
        return executeSync(service.getContractAccountInfos()).getData();
    }

    //获取用户持仓信息
    @Override
    public List<ContractPositionInfo> getContractPositionInfos(String symbol) {
        return executeSync(service.getContractPositionInfos(symbol)).getData();
    }

    @Override
    public List<ContractPositionInfo> getContractPositionInfos() {
        return executeSync(service.getContractPositionInfos()).getData();
    }

    // 交易接口---------------------------------------------------------------------------------
    //合约下单
    @Override
    public long placeOrder(ContractOrderRequest orderRequest) {
        double price = new BigDecimal(orderRequest.getPrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        orderRequest.setPrice(price);
        return executeSync(service.placeOrder(orderRequest)).getData()
                .getOrder_id();
    }

    //撤销订单
    @Override
    public CancelOrderResp cancelOrder(ContractOrderInfoRequest orderInfoRequest) {
        return executeSync(service.cancelOrder(orderInfoRequest)).getData();
    }

    //撤销所有订单
    @Override
    public CancelOrderResp cancelAllOrders(ContractOrderInfoRequest orderInfoRequest) {
        return executeSync(service.cancelAllOrders(orderInfoRequest)).getData();
    }

    //获取合约订单信息
    @Override
    public List<ContractOrderInfo> getContractOrderInfo(ContractOrderInfoRequest orderInfoRequest) {
        return executeSync(service.getContractOrderInfo(orderInfoRequest)).getData();
    }


}

