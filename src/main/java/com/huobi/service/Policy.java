package com.huobi.service;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.POJOs.ContractAccountInfo;
import com.huobi.domain.POJOs.ContractOrderInfo;
import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.domain.enums.OrderStatus;
import com.huobi.domain.request.ContractOrderInfoRequest;
import com.huobi.domain.request.ContractOrderRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.huobi.utils.PrintUtil.print;


/**
 * @Description
 * @Author squirrel
 * @Date 18-10-19 下午4:03
 */
public abstract class Policy {
    protected SimpleDateFormat df;   //设置日期格式
    protected HuobiContractAPI huobiContractAPI;
    protected String policyStartInfo;
    protected List<Long> openPositionHangOrderIDList = new ArrayList<>();
    protected List<Long> closePositionHangOrderIDList = new ArrayList<>();

    public boolean isThisPolicyAvailable;

    //以下为显示系统运行状态的信息
    public double profitRateLong = 0;
    public double profitRateShort = 0;
    public String currentPolicyRunningStatus;   //系统运行时时的状态信息
    public List<String> forceClosePositionStatusList = new ArrayList<>();   //强行平仓时的信息列表
    public List<String> openPositionHangStatusList = new ArrayList<>();  //开平仓挂单时的信息列表
    public List<String> closePositionHangStatusList = new ArrayList<>();  //开平仓挂单时的信息列表
    public List<String> longShortSwitchStatusList = new ArrayList<>();   //系统空多转换时的信息列表


    public Policy(InitSystem initSystem, boolean isThisPolicyAvailable) {
        this.huobiContractAPI = initSystem.huobiContractAPI;
        this.isThisPolicyAvailable = isThisPolicyAvailable;
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    //*********************************************************************************************************************
    //*********************************************************************************************************************
    public abstract void autoTrade();

    //查询可用的保证金
    protected double getAvailableMargin(String symbol, boolean isPercent, double safePercent) {
        List<ContractAccountInfo> contractAccountInfoList = huobiContractAPI.getContractAccountInfos();
        for (ContractAccountInfo contractAccountInfo : contractAccountInfoList) {
            if (contractAccountInfo.getSymbol().equals(symbol)) {
                if (isPercent) {
                    return (contractAccountInfo.getMargin_available() - contractAccountInfo.getMargin_balance() * (1 - safePercent)) / (contractAccountInfo.getMargin_balance() * safePercent);
                }
                return contractAccountInfo.getMargin_available() - contractAccountInfo.getMargin_balance() * (1 - safePercent);
            }
        }
        return 0;
    }

    //查询账户总的保证金
    protected double getTotalMargin(String symbol) {
        List<ContractAccountInfo> contractAccountInfoList = huobiContractAPI.getContractAccountInfos();
        for (ContractAccountInfo contractAccountInfo : contractAccountInfoList) {
            if (contractAccountInfo.getSymbol().equals(symbol)) {
                return contractAccountInfo.getMargin_balance();
            }
        }
        return 0;
    }

    //查询持仓情况
    protected Object queryPosition(String direction, String queryItem) {
        long positionVolume = 0;
        long available = 0;
        double cost_hold = 0;
        double profit_rate = 0;
        List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
        for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
            if (contractPositionInfo.getSymbol().equals("BTC") && contractPositionInfo.getContract_type().equals("quarter") && contractPositionInfo.getDirection().equals(direction)) {
                positionVolume = contractPositionInfo.getVolume();
                available = contractPositionInfo.getAvailable();
                cost_hold = contractPositionInfo.getCost_hold();
                profit_rate = contractPositionInfo.getProfit_rate();
            }
        }
        if (queryItem.equals("volume")) {
            return positionVolume;
        }
        if (queryItem.equals("available")) {
            return available;
        }
        if (queryItem.equals("cost_hold")) {
            return cost_hold;
        }
        if (queryItem.equals("profit_rate")) {
            return profit_rate;
        }
        return "error";
    }

    //计算在一定保证金的情况下，最大可开仓量
    protected long getMaxOpenVolume(double margin, String contractSymbol) {
        double newestPrice = huobiContractAPI.getTrade(contractSymbol).getPrice();
        return (long) (margin * 20 * newestPrice / 100);
    }

    //撤销所有订单
    protected void finishCancelAllOrders() {
        try {
            huobiContractAPI.cancelAllOrders(new ContractOrderInfoRequest(0, "", "BTC"));
            //等待1秒钟，以便撤单完成
            waitSomeMillis(1000);
            openPositionHangOrderIDList.clear();
            closePositionHangOrderIDList.clear();
        } catch (IllegalStateException e) {
        }
    }

    //根据订单ID列表撤销订单
    protected void cancelOrdersAccordingOrderIdList(List<Long> orderList) {
        ContractOrderInfoRequest contractOrderInfoRequest = new ContractOrderInfoRequest(0, "", "BTC");
        for (long orderID : orderList) {
            try {
                contractOrderInfoRequest.setOrder_id(orderID);
                huobiContractAPI.cancelOrder(contractOrderInfoRequest);
                waitSomeMillis(200);
            } catch (IllegalStateException e) {
            }
        }
        orderList.clear();
        //等待1秒钟，以便撤单完成
        waitSomeMillis(1000);
    }

    //根据挂单的订单号查询订单的成交量
    protected long checkHangOrderFinishedVolume(long orderID) {
        ContractOrderInfoRequest orderInfoRequest = new ContractOrderInfoRequest(orderID, "", "BTC");
        ContractOrderInfo orderDetail = huobiContractAPI.getContractOrderInfo(orderInfoRequest).get(0);
        return orderDetail.getTrade_volume();
    }

    //让程序暂停运行几秒钟
    protected void waitSomeSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception e) {
        }
    }

    //让程序暂停运行几毫秒
    protected void waitSomeMillis(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
        }
    }

}
