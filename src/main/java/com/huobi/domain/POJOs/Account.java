package com.huobi.domain.POJOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huobi.domain.enums.AccountState;
import com.huobi.domain.enums.AccountType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 16:02
 */
@Getter
@Setter
public class Account {
    private String id;
    private AccountType type;
    private AccountState state;
    @JsonProperty("user-id")
    private String userId;
    private List<Asset> list;
    private String subtype;
}
