package com.huobi.domain.POJOs.customerPOJOs;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: squirrel
 * @Date: 18-9-11 上午11:07
 */
@Getter
@Setter
public class Wave implements Comparable<Wave>{
    private int fromIndex;
    private int toIndex;
    private String from;
    private String to;
    private double fromPrice;
    private double toPrice;
    private double waveRate;

    @Override
    public int compareTo(Wave o) {
        if (this.fromIndex > o.fromIndex) {
            return -1;
        } else if (this.fromIndex == o.fromIndex && this.toIndex < o.toIndex) {
            return -1;
        } else {
            return 1;
        }
    }
}
