package com.huobi.domain.POJOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 14:16
 */
@Getter
@Setter
public class Merged {

    /**
     * id : 1499225271
     * ts : 1499225271000
     * close : 1885
     * open : 1960
     * high : 1985
     * low : 1856
     * amount : 81486.2926
     * count : 42122
     * vol : 1.57052744857082E8
     * ask : [1885,21.8804]
     * bid : [1884,1.6702]
     */
    private long id;
    private long ts;
    private double close;
    private double open;
    private double high;
    private double low;
    private double amount;
    private int count;
    private double vol;
    private List<Double> ask;
    private List<Double> bid;
    private String version;
}
