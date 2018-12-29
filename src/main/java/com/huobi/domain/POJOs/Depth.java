package com.huobi.domain.POJOs;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 14:39
 */
@Getter
@Setter
public class Depth {

    /**
     * id : 1489464585407
     * ts : 1489464585407
     * bids : [[7964,0.0678],[7963,0.9162]]
     * asks : [[7979,0.0736],[8020,13.6584]]
     */
    private String id;
    private String ts;
    private List<List<Double>> bids;
    private List<List<Double>> asks;
    private String version;
}
