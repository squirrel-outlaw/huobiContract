package com.huobi.api;

import com.huobi.constant.HuobiConsts;
import com.huobi.domain.POJOs.*;
import com.huobi.domain.request.ContractOrderInfoRequest;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.domain.response.OrderResp;
import com.huobi.domain.response.RespBody;
import com.huobi.domain.response.RespTick;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Set;

/**
 * created by jacky. 2018/7/20 8:41 PM
 */
public interface HuobiContractApiService {

    // 行情接口---------------------------------------------------------------------------------
    //获取 Market Depth 数据
    @GET("/market/depth")
    Call<RespBody<Depth>> getDepth(@Query("symbol") String symbol, @Query("type") String type);

    //获取K线数据
    @GET("/market/history/kline")
    Call<RespBody<List<Kline>>> getKlines(@Query("symbol") String symbol, @Query("period") String period, @Query("size") String size);

    //获取聚合行情(Ticker)
    @GET("/market/detail/merged")
    Call<RespBody<Merged>> getMerged(@Query("symbol") String symbol);

    //获取最新成交记录
    @GET("/market/trade")
    Call<RespBody<RespTick<Trade>>> getTrade(@Query("symbol") String symbol);

    //批量获取最近的交易记录
    @GET("/market/history/trade")
    Call<RespBody<List<RespTick<Trade>>>> getHistoryTrades(@Query("symbol") String symbol, @Query("size") String size);

    // 资产接口---------------------------------------------------------------------------------
    //获取用户账户信息
    @Headers(HuobiConsts.ENDPOINT_SECURITY_TYPE_SIGNED_HEADER)
    @POST("/api/v1/contract_account_info")
    Call<RespBody<List<ContractAccountInfo>>> getContractAccountInfos();

    //获取用户持仓信息
    @Headers(HuobiConsts.ENDPOINT_SECURITY_TYPE_SIGNED_HEADER)
    @POST("/api/v1/contract_position_info")
    Call<RespBody<List<ContractPositionInfo>>> getContractPositionInfos(@Query("symbol") String symbol);

    //获取用户持仓信息(缺省参数，默认返回所有品种）
    @Headers(HuobiConsts.ENDPOINT_SECURITY_TYPE_SIGNED_HEADER)
    @POST("/api/v1/contract_position_info")
    Call<RespBody<List<ContractPositionInfo>>> getContractPositionInfos();

    // 交易接口---------------------------------------------------------------------------------
    //合约下单
    @Headers(HuobiConsts.ENDPOINT_SECURITY_TYPE_SIGNED_HEADER)
    @POST("/api/v1/contract_order")
    Call<RespBody<OrderResp>> placeOrder(@Body ContractOrderRequest contractOrderRequest);

    //获取合约订单信息
    @Headers(HuobiConsts.ENDPOINT_SECURITY_TYPE_SIGNED_HEADER)
    @POST("/api/v1/contract_order_info")
    Call<RespBody<ContractOrderInfo>> getContractOrderInfo(@Body ContractOrderInfoRequest orderInfoRequest);


}
