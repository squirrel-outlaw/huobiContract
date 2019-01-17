package com.huobi.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-17 下午1:02
 */
@AllArgsConstructor
public enum OrderStatus {
    PREPARE_SUBMIT(1),     //准备提交
    PREPARE_SUBMIT_2(2),  //准备提交
    SUBMITTED(3),           //已提交
    PARTIAL_FILLED(4),   //部分成交
    PARTIAL_CANCELED(5), //部分成交已撤单
    FILLED(6),           //全部成交
    CANCELED(7),         //已撤单
    CANCELING(11);       //撤单中

    private int code;

    @JsonValue
    public int getCode() {
        return code;
    }


}
