package com.huobi.domain.POJOs;

import com.huobi.domain.enums.ContractType;
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
    private String contract_type;   //合约类型
    private long volume;             //持仓量
    private long available;         //可平仓数量
    private long frozen;             //冻结数量
    private double cost_open;          //开仓均价
    private double cost_hold;          //持仓均价
    private double profit_unreal;      //未实现盈亏
    private double profit_rate;        //收益率
    private double profit;              //收益
    private double position_margin;    //持仓保证金
    private int lever_rate;                //杠杠倍数
    private String direction;               //买卖方向
}
