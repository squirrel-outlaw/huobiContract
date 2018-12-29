package com.huobi.api;

import com.huobi.domain.POJOs.*;
import com.huobi.domain.enums.*;
import com.huobi.domain.response.BatchCancelResp;

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
    Trade getTrade(String symbol);

    //批量获取最近的交易记录
    List<Trade> getHistoryTrades(String symbol, String size);

// 资产接口---------------------------------------------------------------------------------

    //获取用户账户信息
    List<ContractAccountInfo> getContractAccountInfos(String symbol);

    //获取用户持仓信息
    List<ContractPositionInfo> getContractPositionInfos(String symbol);

// 交易接口---------------------------------------------------------------------------------





}
