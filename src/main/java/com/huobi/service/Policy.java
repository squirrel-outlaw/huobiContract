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
    protected ActualOrderHandler actualOrderHandler;
    protected HuobiContractAPI huobiContractAPI;

    public Policy(DataManager dataManager, ActualOrderHandler actualOrderHandler) {
        this.dataManager = dataManager;
        this.actualOrderHandler=actualOrderHandler;
        this.huobiContractAPI=dataManager.initSystem.huobiContractAPI;
    }

    public abstract List<ContractOrderRequest> generateContractOrderRequest();

    //计算合约价格涨跌幅变化率
    public double calculateContractPriceRateDerivative(){
        double contractPriceRateLast = 0;
        double contractPriceRate2rdLast = 0;
        if (dataManager.BTCPriceRateList.size() >= 2) {
            contractPriceRateLast = dataManager.BTCPriceRateList.get(dataManager.BTCPriceRateList.size() - 1);
            contractPriceRate2rdLast = dataManager.BTCPriceRateList.get(dataManager.BTCPriceRateList.size() - 2);
        }
        return contractPriceRateLast-contractPriceRate2rdLast;
    }


}
