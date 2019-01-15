package com.huobi.service.policyImpl;


import com.huobi.domain.POJOs.ContractAccountInfo;
import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.DataManager;
import com.huobi.service.Policy;

import java.util.ArrayList;
import java.util.List;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.utils.PrintUtil.print;


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

        //
        if (getAvailableMarginPercent("BTC") < OPEN_POSITION_AVAILABLE_MARGIN_PERCENT) {
            print(getAvailableMarginPercent("BTC"));
            return contractOrderRequestList;
        }
        double BTCPriceRateLast = 0;
        double BTCPriceRate2rdLast = 0;
        if (dataManager.BTCPriceRateList.size() >= 2) {
            BTCPriceRateLast = dataManager.BTCPriceRateList.get(dataManager.BTCPriceRateList.size() - 1);
            BTCPriceRate2rdLast = dataManager.BTCPriceRateList.get(dataManager.BTCPriceRateList.size() - 2);
        }
        print(BTCPriceRateLast - BTCPriceRate2rdLast);
        if (BTCPriceRateLast - BTCPriceRate2rdLast > OPEN_LONG_POSITION_RATE_DERIVATIVE) {
            //获取合约的最新价格
            double newestPrice = huobiContractAPI.getTrade("BTC_CQ").getPrice();
            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", "", "",
                    newestPrice, 1, "buy", "open", 20, "limit");
            contractOrderRequestList.add(contractOrderRequest);
        }else if(BTCPriceRateLast - BTCPriceRate2rdLast < OPEN_SHORT_POSITION_RATE_DERIVATIVE){
            double newestPrice = huobiContractAPI.getTrade("BTC_CQ").getPrice();
            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", "", "",
                    newestPrice, 1, "sell", "open", 20, "limit");
            contractOrderRequestList.add(contractOrderRequest);
        }
        return contractOrderRequestList;
    }

    //查询可用的保证金占总资产的百分比
    public double getAvailableMarginPercent(String symbol) {
        List<ContractAccountInfo> contractAccountInfoList = huobiContractAPI.getContractAccountInfos();
        for (ContractAccountInfo contractAccountInfo : contractAccountInfoList) {
            if (contractAccountInfo.getSymbol().equals(symbol)) {
                return contractAccountInfo.getMargin_available() / contractAccountInfo.getMargin_balance();
            }
        }
        return 0;
    }


}
