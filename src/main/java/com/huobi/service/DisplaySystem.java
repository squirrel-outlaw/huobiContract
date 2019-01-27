package com.huobi.service;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.POJOs.ContractAccountInfo;
import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.service.policyImpl.PolicyWave;

import java.util.List;

/**
 * @Author: squirrel
 * @Date: 18-9-17 下午4:32
 */
public class DisplaySystem {
    private HuobiContractAPI huobiContractAPI;
    private PolicyWave policyWave;


    public String functionDisplay = "\n" +
            "功能提示:\n" +
            "1.查询账户总资产\n" +
            "2.查询账户持仓保证金\n" +
            "3.当前持仓收益率\n" +
            "0:返回\n" +
            "q:退出系统，终止程序\n";


    public DisplaySystem(InitSystem initSystem, PolicyWave policyWave) {
        this.huobiContractAPI = initSystem.huobiContractAPI;
        this.policyWave = policyWave;
    }


    //显示账户基本情况
    public String displayAccountInformation(String symbol, String queryItem) {
        double margin_balance = 0;
        double margin_position = 0;
        List<ContractAccountInfo> contractAccountInfoList = huobiContractAPI.getContractAccountInfos();
        for (ContractAccountInfo contractAccountInfo : contractAccountInfoList) {
            if (contractAccountInfo.getSymbol().equals(symbol)) {
                margin_balance = contractAccountInfo.getMargin_balance();
                margin_position = contractAccountInfo.getMargin_position();
            }
        }
        if (queryItem.equals("margin_balance")) {
            return "账户总资产为: " + margin_balance + " " + symbol;
        }
        if (queryItem.equals("margin_position")) {
            return "持仓保证金为: " + margin_position + " " + symbol;
        }
        return "查询错误";
    }

    //查询当前持仓收益率
    public String queryPositionProfitRate(String direction) {
        double profit_rate = 0;
        List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
        for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
            if (contractPositionInfo.getSymbol().equals("BTC") && contractPositionInfo.getContract_type().equals("quarter") && contractPositionInfo.getDirection().equals(direction)) {
                profit_rate = contractPositionInfo.getProfit_rate();
            }
        }
        return "当前持仓 " + direction + " 收益率为: " + profit_rate;
    }


}
