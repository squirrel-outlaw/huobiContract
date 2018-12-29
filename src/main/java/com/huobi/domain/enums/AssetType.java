package com.huobi.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * created by jacky. 2018/7/23 4:11 PM
 */

//@AllArgsConstructor: 自动生成全参数构造函数。
@AllArgsConstructor
public enum AssetType {
    WORKING("working"),
    TRADE("trade"),
    FROZEN("frozen"),
    LOAN("loan"),
    INTEREST("interest"),
    TRANSFER_OUT_AVAILABLE("transfer_out_available"),
    LOAN_AVAILABLE("loan_available");
    private String name;


    @JsonValue
    public String getName() {
        return name;
    }

}
