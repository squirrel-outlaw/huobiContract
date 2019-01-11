package com.huobi.service;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.policyImpl.PolicyClosePosition;
import com.huobi.service.policyImpl.PolicyOpenByPriceRate;
import java.util.*;

import static com.huobi.constant.TradeConditionConsts.*;

/**
 * @Author: squirrel
 * @Date: 18-9-19 上午9:11
 */
public class TradeSystem {
    private DataManager dataManager;
    private HuobiContractAPI huobiContractAPI;

    public TradeSystem(DataManager dataManager) {
        this.dataManager = dataManager;
        this.huobiContractAPI = dataManager.initSystem.huobiContractAPI;
    }

    public void autoTrade() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                //根据开仓策略下订单
                Policy policyOpenByPriceRate = new PolicyOpenByPriceRate(dataManager);
                List<ContractOrderRequest> openRequestlist = policyOpenByPriceRate.generateContractOrderRequest();
                long orderID = huobiContractAPI.placeOrder(openRequestlist.get(0));
                //根据平仓策略下订单
                Policy policyClosePosition=new PolicyClosePosition(dataManager);
                List<ContractOrderRequest> closeRequestList = policyClosePosition.generateContractOrderRequest();
                for (ContractOrderRequest closeContractOrderRequest:closeRequestList){
                    huobiContractAPI.placeOrder(closeContractOrderRequest);
                }
            }
        }, 0, AUTO_TRADE_INTERVAL);// 自动交易间隔为1s
    }

}

