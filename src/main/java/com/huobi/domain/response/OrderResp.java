package com.huobi.domain.response;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-7 下午3:51
 */
@Getter
@Setter
public class OrderResp {
    int index;
    long order_id;
    long client_order_id;
}
