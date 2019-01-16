package com.huobi.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-16 下午4:28
 */
@AllArgsConstructor
@Getter
@Setter
public class ContractOrderInfoRequest {
    private long order_id;
    private String client_order_id;
    private String symbol;   //"BTC","ETH"..
}
