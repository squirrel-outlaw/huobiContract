package com.huobi.service.policyImpl;

import com.huobi.domain.POJOs.Account;
import com.huobi.domain.POJOs.customerPOJOs.Position;
import com.huobi.domain.POJOs.customerPOJOs.TradeSignal;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.DataManager;
import com.huobi.service.Policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.huobi.constant.TradeConditionConsts.*;

/**
 * @Description
 * @Author squirrel
 * @Date 18-10-19 下午4:18
 */
public class PolicyOne extends Policy {

    public PolicyOne(DataManager dataManager) {
        super(dataManager);
    }

    public ContractOrderRequest generateContractOrderRequest() {
        ContractOrderRequest contractOrderRequest = new ContractOrderRequest();
        double BTCPriceRateLast = 0;
        double BTCPriceRate2rdLast = 0;
        if (dataManager.BTCPriceRateList.size() >= 2) {
            BTCPriceRateLast = dataManager.BTCPriceRateList.get(dataManager.BTCPriceRateList.size() - 1);
            BTCPriceRate2rdLast = dataManager.BTCPriceRateList.get(dataManager.BTCPriceRateList.size() - 2);
        }
        if (BTCPriceRateLast - BTCPriceRate2rdLast > 0.5) {
            contractOrderRequest.setSymbol("BTC");
            contractOrderRequest.setContract_type("this_week");
            contractOrderRequest.setPrice();
            contractOrderRequest.setVolume();
            contractOrderRequest.setDircetion("buy");
            contractOrderRequest.setOffset("open");
            contractOrderRequest.setLever_rate(20);
            contractOrderRequest.setOrder_price_type( "limit");
        }
        return contractOrderRequest;
    }
}
