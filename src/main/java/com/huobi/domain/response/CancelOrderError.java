package com.huobi.domain.response;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-17 上午11:20
 */
@Getter
@Setter
public class CancelOrderError {
    long order_id;
    String err_code;
    String err_msg;
}
