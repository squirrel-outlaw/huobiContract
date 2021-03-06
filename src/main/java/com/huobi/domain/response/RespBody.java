package com.huobi.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * created by jacky. 2018/7/21 11:16 AM
 * data数据返回的都是List
 */
@Getter
@Setter
public class RespBody<T> {
    private String status;
    private long ts;
    private String ch;
    private T data;
    private T tick;
    @JsonProperty("err_code")
    private String errCode;
    @JsonProperty("err_msg")
    private String errMsg;

    public String toErrorString() {
        String msg = "errCode:%s, errMsg:%s.";
        return String.format(msg, errCode, errMsg);
    }

}
