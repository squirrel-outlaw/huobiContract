package com.huobi.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * created by jacky. 2018/9/21 3:16 PM
 */
@Setter
@Getter
public class BatchCancelResp {
    private Set<String> success;
    private Set<FailedObj> failed;
}



