package com.huobi.api;

import com.huobi.domain.POJOs.*;
import com.huobi.domain.enums.*;
import com.huobi.domain.request.ContractOrderInfoRequest;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.domain.response.CancelOrderResp;


import java.util.List;

public interface HuobiContractAPI {
    // 行情接口---------------------------------------------------------------------------------
    //获取 Market Depth 数据
    Depth getDepth(String symbol, MergeLevel step);

    //获取K线数据
    List<Kline> getKlines(String symbol, Resolution period, String size);

    //获取聚合行情(Ticker)
    Merged getMerged(String symbol);

    //获取最新成交记录
    //参数symbol为"BTC_CW"，"BTC_NW"，"BTC_CQ"
    Trade getTrade(String symbol);

    //批量获取最近的交易记录
    List<Trade> getHistoryTrades(String symbol, String size);

    // 资产接口---------------------------------------------------------------------------------
    //获取用户账户信息
    List<ContractAccountInfo> getContractAccountInfos();

    //获取用户持仓信息
    List<ContractPositionInfo> getContractPositionInfos(String symbol);

    List<ContractPositionInfo> getContractPositionInfos();

    // 交易接口---------------------------------------------------------------------------------
    //合约下单
    long placeOrder(ContractOrderRequest orderRequest);

    //撤销订单
    CancelOrderResp cancelOrder(ContractOrderInfoRequest orderInfoRequest);

    //撤销所有订单
    CancelOrderResp cancelAllOrders(ContractOrderInfoRequest orderInfoRequest);

    //获取合约订单信息
    List<ContractOrderInfo> getContractOrderInfo(ContractOrderInfoRequest orderInfoRequest);


}
