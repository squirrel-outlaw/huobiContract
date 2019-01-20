package com.huobi.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Description
 * @Author squirrel
 * @Date 18-12-29 下午12:42
 */
@AllArgsConstructor
@Getter
@Setter
public class ContractOrderRequest {

    private String symbol;                 //品种代码
    private String contract_type;         //合约类型
    private String contract_code;         //合约代码
    private String client_order_id;       //客户自己填写和维护，这次一定要大于上一次
    private double price;                //价格
    private long volume;                 //委托数量(张)
    private String direction;             //"buy":买 "sell":卖
    private String offset;                 //"open":开 "close":平
    private int lever_rate;               //杠杠倍数
    private String order_price_type;      //订单报价类型 "limit":限价 "opponent":对手价

    //拷贝构造方式
    public ContractOrderRequest(ContractOrderRequest contractOrderRequest) {
        this.symbol = contractOrderRequest.symbol;
        this.contract_type = contractOrderRequest.contract_type;
        this.contract_code = contractOrderRequest.contract_code;
        this.client_order_id = contractOrderRequest.client_order_id;
        this.price = contractOrderRequest.price;
        this.volume = contractOrderRequest.volume;
        this.direction = contractOrderRequest.direction;
        this.offset = contractOrderRequest.offset;
        this.lever_rate = contractOrderRequest.lever_rate;
        this.order_price_type = contractOrderRequest.order_price_type;
    }

}
