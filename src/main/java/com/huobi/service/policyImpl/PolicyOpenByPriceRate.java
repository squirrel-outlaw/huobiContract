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
        //如果可用仓位已经小于限制，怎不进行开仓
        if (getAvailableMarginPercent("BTC") < OPEN_POSITION_AVAILABLE_MARGIN_PERCENT) {
            print(getAvailableMarginPercent("BTC"));
            return contractOrderRequestList;
        }
        //计算合约价格涨跌幅变化率
        double contractPriceRateDerivative=calculateContractPriceRateDerivative();
        print(contractPriceRateDerivative);
        if (contractPriceRateDerivative > OPEN_LONG_POSITION_RATE_DERIVATIVE) {
            //获取合约的最新价格
            double newestPrice = huobiContractAPI.getTrade("BTC_CQ").getPrice();
            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", "", "",
                    newestPrice, 1, "sell", "open", 20, "limit");
            contractOrderRequestList.add(contractOrderRequest);
        } else if (contractPriceRateDerivative < OPEN_SHORT_POSITION_RATE_DERIVATIVE) {
            double newestPrice = huobiContractAPI.getTrade("BTC_CQ").getPrice();
            ContractOrderRequest contractOrderRequest = new ContractOrderRequest("BTC", "quarter", "", "",
                    newestPrice, 1, "buy", "open", 20, "limit");
            contractOrderRequestList.add(contractOrderRequest);
        }
        return contractOrderRequestList;
    }

    //查询可用的保证金占总资产的百分比
    private double getAvailableMarginPercent(String symbol) {
        List<ContractAccountInfo> contractAccountInfoList = huobiContractAPI.getContractAccountInfos();
        for (ContractAccountInfo contractAccountInfo : contractAccountInfoList) {
            if (contractAccountInfo.getSymbol().equals(symbol)) {
                return contractAccountInfo.getMargin_available() * 100 / contractAccountInfo.getMargin_balance();
            }
        }
        return 0;
    }


}
