package com.huobi.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * created by jacky. 2018/7/23 8:26 PM
 */

//@AllArgsConstructor: 自动生成全参数构造函数。
@AllArgsConstructor
public enum Resolution {
    M1("1min"),
    M5("5min"),
    M15("15min"),
    M30("30min"),
    M60("60min"),
    M240("4hour"),
    D1("1day"),
    MN1("1mon"),
    W1("1week"),
    Y1("1year");
    private String code;

    @JsonValue
    public String getCode() {
        return code;
    }
}
