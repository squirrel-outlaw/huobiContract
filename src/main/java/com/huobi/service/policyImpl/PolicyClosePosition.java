package com.huobi.service.policyImpl;

import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.domain.enums.ContractType;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.DataManager;
import com.huobi.service.Policy;

import java.util.ArrayList;
import java.util.List;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.service.BasicFunction.getContractSymbol;
import static com.huobi.utils.PrintUtil.print;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-8 上午11:14
 */
public class PolicyClosePosition extends Policy {

    public PolicyClosePosition(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public List<ContractOrderRequest> generateContractOrderRequest() {
        List<ContractOrderRequest> contractOrderRequestList = new ArrayList<>();
        List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
        for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
            //如果没有可用的持仓，则不处理
            if (contractPositionInfo.getAvailable() == 0) {
                continue;
            }
            //创建contractSymbol
            String contractSymbol = getContractSymbol(contractPositionInfo.getSymbol() ,contractPositionInfo.getContract_type());
            //获取合约的最新价格
            double newestPrice = huobiContractAPI.getTrade(contractSymbol).getPrice();
            //获取持仓成本价
            double costPrice = contractPositionInfo.getCost_hold();
            //盈亏比例
            double profitLossRate = (newestPrice - costPrice) * 100 / costPrice;
            print(profitLossRate);

            double contractPriceRateDerivative = calculateContractPriceRateDerivative();
            if (contractPositionInfo.getDirection().equals("buy")) {
                //做多时的平仓策略
                if ((profitLossRate > TAKE_PROFIT_RATE || profitLossRate < STOP_LOSS_RATE)
                        && contractPriceRateDerivative < CLOSE_LONG_POSITION_RATE_DERIVATIVE_LIMIT) {

                    ContractOrderRequest contractOrderRequest = new ContractOrderRequest(contractPositionInfo
                            .getSymbol(), contractPositionInfo.getContract_type(), null, "", newestPrice,
                            contractPositionInfo.getAvailable(), "sell", "close", contractPositionInfo.getLever_rate(),
                            "limit");
                    contractOrderRequestList.add(contractOrderRequest);
                }
            } else {
                //做空时的平仓策略
                if (((0 - profitLossRate) > TAKE_PROFIT_RATE || (0 - profitLossRate) < STOP_LOSS_RATE) &&
                        contractPriceRateDerivative > CLOSE_SHORT_POSITION_RATE_DERIVATIVE_LIMIT) {
                    ContractOrderRequest contractOrderRequest = new ContractOrderRequest(contractPositionInfo
                            .getSymbol(), contractPositionInfo.getContract_type(), null, "", newestPrice,
                            contractPositionInfo.getAvailable(), "buy", "close", contractPositionInfo.getLever_rate(),
                            "limit");
                    contractOrderRequestList.add(contractOrderRequest);
                }
            }
        }
        return contractOrderRequestList;
    }
}
