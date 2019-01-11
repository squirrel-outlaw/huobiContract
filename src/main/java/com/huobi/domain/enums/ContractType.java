package com.huobi.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * created by jacky. 2018/7/23 4:11 PM
 */

//@AllArgsConstructor: 自动生成全参数构造函数。
@AllArgsConstructor
public enum ContractType {
    this_week("CW"),
    next_week("NW"),
    quarter("CQ");
    private String type;

    @JsonValue
    public String getType() {
        return type;
    }
}
