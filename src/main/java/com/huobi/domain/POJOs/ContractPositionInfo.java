package com.huobi.domain.POJOs;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Description  用户持仓信息
 * @Author squirrel
 * @Date 18-12-29 下午12:25
 */
@Getter
@Setter
public class ContractPositionInfo {
    private String symbol;                 //品种代码
    private String contract_code;         //合约代码
    private String contract_type;         //合约类型
    private BigDecimal volume;             //持仓量
    private BigDecimal available;         //可平仓数量
    private BigDecimal frozen;             //冻结数量
    private BigDecimal cost_open;          //开仓均价
    private BigDecimal cost_hold;          //持仓均价
    private BigDecimal profit_unreal;      //未实现盈亏
    private BigDecimal profit_rate;        //收益率
    private BigDecimal profit;              //收益
    private BigDecimal position_margin;    //持仓保证金
    private int lever_rate;                //杠杠倍数
    private String direction;               //买卖方向
}
