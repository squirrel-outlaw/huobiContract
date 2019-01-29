package com.huobi.service;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.POJOs.ContractAccountInfo;
import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.service.policyImpl.PolicyByLead;
import com.huobi.service.policyImpl.PolicyWave;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.huobi.constant.TradeConditionConsts.AUTO_TRADE_INTERVAL;

/**
 * @Author: squirrel
 * @Date: 18-9-17 下午4:32
 */
public class DisplaySystem {
    private HuobiContractAPI huobiContractAPI;
    private Policy policy;
    public boolean isDisplayPolicyRunningStatus = false;


    public DisplaySystem(InitSystem initSystem, Policy policy) {
        this.huobiContractAPI = initSystem.huobiContractAPI;
        this.policy = policy;
    }

    public void displayFunction() {
        System.out.println("\n" +
                "功能提示:\n" +
                "1.查询账户基本情况\n" +
                "2.当前策略运行情况\n" +
                "3.当前策略强行平仓情况\n" +
                "4.当前策略开平仓挂单情况\n" +
                "0:返回\n" +
                "q:退出系统，终止程序\n");
    }

    //显示账户基本情况
    public void displayAccountInformation(String symbol) {
        double margin_balance = 0;
        double margin_position = 0;
        double profit_rate_buy = 0;
        double profit_rate_sell = 0;
        List<ContractAccountInfo> contractAccountInfoList = huobiContractAPI.getContractAccountInfos();
        for (ContractAccountInfo contractAccountInfo : contractAccountInfoList) {
            if (contractAccountInfo.getSymbol().equals(symbol)) {
                margin_balance = contractAccountInfo.getMargin_balance();
                margin_position = contractAccountInfo.getMargin_position();
            }
        }
        List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
        for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
            if (contractPositionInfo.getSymbol().equals(symbol) && contractPositionInfo.getContract_type().equals
                    ("quarter") && contractPositionInfo.getDirection().equals("buy")) {
                profit_rate_buy = contractPositionInfo.getProfit_rate();
            }
            if (contractPositionInfo.getSymbol().equals(symbol) && contractPositionInfo.getContract_type().equals
                    ("quarter") && contractPositionInfo.getDirection().equals("sell")) {
                profit_rate_sell = contractPositionInfo.getProfit_rate();
            }
        }
        System.out.println("\n" +
                "账户总资产为: " + margin_balance + " " + symbol + "\n" +
                "持仓保证金为: " + margin_position + " " + symbol + "\n" +
                "当前买多，收益率为: " + profit_rate_buy + "\n" +
                "当前卖空，收益率为: " + profit_rate_sell + "\n");
    }

    //显示策略运行时，基本情况
    public void displayPolicyRunningStatus() {
        System.out.println(policy.policyStartInfo + "\n");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if (!isDisplayPolicyRunningStatus) {
                    timer.cancel();
                }
                System.out.println(policy.currentPolicyRunningStatus + "\n");
            }
        }, 0, AUTO_TRADE_INTERVAL);// 自动交易间隔时间
    }


    //显示策略运行时，强行平仓的相关信息
    public void displayForceClosePositionInfo() {
        if (!policy.forceClosePositionStatusList.isEmpty()) {
            for (String string : policy.forceClosePositionStatusList) {
                System.out.println(string + "\n");
            }
            return;
        }
        System.out.println("没有出现强行平仓" + "\n");
    }

    //显示策略运行时，多空转换的状态信息
    public void displayLongShortSwitchStatusInfo() {
        if (!policy.longShortSwitchStatusList.isEmpty()) {
            for (String string : policy.longShortSwitchStatusList) {
                System.out.println(string + "\n");
            }
            return;
        }
        System.out.println("没有出现多空转换" + "\n");
    }

    //显示策略运行时，开平仓挂单的状态信息
    public void displayOpenClosePositionHangStatusInfo() {
        if (!policy.openClosePositionHangStatusList.isEmpty()) {
            for (String string : policy.openClosePositionHangStatusList) {
                System.out.println(string + "\n");
            }
            return;
        }
        System.out.println("没有出现开平仓挂单" + "\n");
    }

}
