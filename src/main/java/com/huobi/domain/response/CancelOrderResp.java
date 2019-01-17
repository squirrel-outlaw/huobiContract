package com.huobi.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-17 上午11:17
 */

@Getter
@Setter
public class CancelOrderResp {
    List<CancelOrderError> errors;
    String successes;
}
