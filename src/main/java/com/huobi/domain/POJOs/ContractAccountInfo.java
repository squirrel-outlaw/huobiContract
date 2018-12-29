package com.huobi.domain.POJOs;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Description   用户持仓信息
 * @Author squirrel
 * @Date 18-12-28 下午4:44
 */
@Getter
@Setter
public class ContractAccountInfo {
    private String symbol;                 //品种代码
    private BigDecimal margin_balance;    //账户权益
    private BigDecimal margin_position;   //持仓保证金（当前持有仓位所占用的保证金）
    private BigDecimal margin_frozen;     //冻结保证金
    private BigDecimal margin_available;  //可用保证金
    private BigDecimal profit_real;        //已实现盈亏
    private BigDecimal profit_unreal;      //未实现盈亏
    private BigDecimal risk_rate;           //保证金率
    private BigDecimal liquidation_price;  //预估爆仓价
    private BigDecimal withdraw_available; //可划转数量
    private int lever_rate;                 //杠杠倍数

}

