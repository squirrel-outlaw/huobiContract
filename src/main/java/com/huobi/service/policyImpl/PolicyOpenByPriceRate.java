package com.huobi.service.policyImpl;


import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.DataManager;
import com.huobi.service.Policy;

import java.util.ArrayList;
import java.util.List;


/**
 * @Description
 * @Author squirrel
 * @Date 18-10-19 下午4:18
 */
public class PolicyOpenByPriceRate extends Policy {

    public PolicyOpenByPriceRate(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public List<ContractOrderRequest> generateContractOrderRequest() {
        List<ContractOrderRequest> contractOrderRequestList = new ArrayList<>();
        double BTCPriceRateLast = 0;
        double BTCPriceRate2rdLast = 0;
        if (dataManager.BTCPriceRateList.size() >= 2) {
            BTCPriceRateLast = dataManager.BTCPriceRateList.get(dataManager.BTCPriceRateList.size() - 1);
            BTCPriceRate2rdLast = dataManager.BTCPriceRateList.get(dataManager.BTCPriceRateList.size() - 2);
        }
        if (BTCPriceRateLast - BTCPriceRate2rdLast > 0.5) {
            //获取合约的最新价格
            double newestPrice = huobiContractAPI.getTrade("BTC_CW").getPrice();
           // ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "this_week", null, 0,
              //      newestPrice,
           //         1, "buy", "open", 20,
            //        "limit");
            //contractOrderRequestList.add(contractOrderRequest);
        }
        return contractOrderRequestList;
    }
}
