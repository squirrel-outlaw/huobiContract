package com.huobi.domain.POJOs;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 11:35
 */
@Getter
@Setter
public class Kline {
    private long id;
    private String realtime;
    private int count;
    private double open;
    private double high;
    private double low;
    private double close;
    private BigDecimal amount;
    private BigDecimal vol;
    private String version;
    //用来存放自定义的MA和MA_Derivative
    private double MA;
    private double MA_Derivative;
}
