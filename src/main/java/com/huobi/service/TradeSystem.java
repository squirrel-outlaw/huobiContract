package com.huobi.service;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.service.policyImpl.PolicyClosePosition;
import com.huobi.service.policyImpl.PolicyOpenByPriceRate;

import java.util.*;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.utils.PrintUtil.print;

/**
 * @Author: squirrel
 * @Date: 18-9-19 上午9:11
 */
public class TradeSystem {
    private DataManager dataManager;
    public HuobiContractAPI huobiContractAPI;

    public TradeSystem(DataManager dataManager) {
        this.dataManager = dataManager;
        this.huobiContractAPI = dataManager.initSystem.huobiContractAPI;
    }

    public void autoTrade() {
        ActualOrderHandler actualOrderHandler = new ActualOrderHandler(huobiContractAPI);

        Policy policyClosePosition = new PolicyClosePosition(dataManager);
        Policy policyOpenByPriceRate = new PolicyOpenByPriceRate(dataManager);


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    //根据平仓策略下订单
                    List<ContractOrderRequest> closeRequestList = policyClosePosition.generateContractOrderRequest();
                    for (ContractOrderRequest closeContractOrderRequest : closeRequestList) {
                        if (actualOrderHandler.actualRequestOrder == null) {
                            actualOrderHandler.actualRequestOrder = closeContractOrderRequest;
                        }
                    }

                    //根据开仓策略下订单
                    List<ContractOrderRequest> openRequestlist = policyOpenByPriceRate.generateContractOrderRequest();
                    if (openRequestlist.size() > 0) {
                        if (actualOrderHandler.actualRequestOrder == null) {
                            actualOrderHandler.actualRequestOrder = openRequestlist.get(0);
                        }
                    }
                } catch (IllegalStateException e) {
                }
            }
        }, 0, AUTO_TRADE_INTERVAL);// 自动交易间隔为1s
    }


}

