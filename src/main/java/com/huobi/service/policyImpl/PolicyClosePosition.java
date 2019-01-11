package com.huobi.service.policyImpl;

import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.domain.enums.ContractType;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.DataManager;
import com.huobi.service.Policy;

import java.util.ArrayList;
import java.util.List;

import static com.huobi.constant.TradeConditionConsts.*;

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
            //创建contractType枚举把"this_week"转化为"CW"
            ContractType contractType = ContractType.valueOf(contractPositionInfo.getContract_type());
            String symbol = contractPositionInfo.getSymbol() + "_" + contractType.getType();
            //获取合约的最新价格
            double newestPrice = huobiContractAPI.getTrade(symbol).getPrice();
            //获取持仓成本价
            double costPrice = contractPositionInfo.getCost_hold();
            //盈亏比例
            double profitLossRate = (newestPrice - costPrice) / costPrice;
            if (contractPositionInfo.getDirection().equals("buy")) {
                if (profitLossRate > TAKE_PROFIT_RATE || profitLossRate < STOP_LOSS_RATE) {
                    //做多时的平仓策略
                  //  ContractOrderRequest contractOrderRequest = new ContractOrderRequest(contractPositionInfo
                   //         .getSymbol(), contractPositionInfo.getContract_type(), null, 0, newestPrice,
                    //        contractPositionInfo.getVolume(), "sell", "close", contractPositionInfo.getLever_rate(),
                     //       "limit");
                  //  contractOrderRequestList.add(contractOrderRequest);
                }
            } else {
                if (profitLossRate < TAKE_PROFIT_RATE || profitLossRate > STOP_LOSS_RATE) {
                    //做空时的平仓策略
                 //   ContractOrderRequest contractOrderRequest = new ContractOrderRequest(contractPositionInfo
                         //   .getSymbol(), contractPositionInfo.getContract_type(), null, 0, newestPrice,
                       //     contractPositionInfo.getVolume(), "buy", "close", contractPositionInfo.getLever_rate(),
                   //         "limit");
                  //  contractOrderRequestList.add(contractOrderRequest);
                }
            }
        }
        return contractOrderRequestList;
    }
}
