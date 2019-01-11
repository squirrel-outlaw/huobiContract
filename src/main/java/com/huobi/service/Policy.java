package com.huobi.service;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.request.ContractOrderRequest;

import java.util.List;


/**
 * @Description
 * @Author squirrel
 * @Date 18-10-19 下午4:03
 */
public abstract class Policy {

    protected DataManager dataManager;
    protected HuobiContractAPI huobiContractAPI;

    public Policy(DataManager dataManager) {
        this.dataManager = dataManager;
        this.huobiContractAPI=dataManager.initSystem.huobiContractAPI;
    }

    public abstract List<ContractOrderRequest> generateContractOrderRequest();

}
