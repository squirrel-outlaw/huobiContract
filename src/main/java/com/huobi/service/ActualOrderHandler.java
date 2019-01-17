package com.huobi.service;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.POJOs.ContractOrderInfo;
import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.domain.enums.MergeLevel;
import com.huobi.domain.enums.OrderStatus;
import com.huobi.domain.request.ContractOrderInfoRequest;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.domain.response.CancelOrderResp;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import static com.huobi.constant.TradeConditionConsts.HANG_ORDER_INTERVAL;
import static com.huobi.service.BasicFunction.getContractSymbol;
import static com.huobi.utils.PrintUtil.print;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-16 下午4:06
 */


public class ActualOrderHandler {

    //借用Order对象的属性，虚拟的完成订单对象，用来保存最终完成交易的详情
    //借用amount，price，filledCashAmount这三个属性值，其中filledCashAmount是成交计价货币使用量，包含手续费
    private HuobiContractAPI huobiContractAPI;

    private ContractOrderRequest virtualRequestOrder;
    public ContractOrderRequest actualRequestOrder;
    private long orderID;

    public ActualOrderHandler(HuobiContractAPI huobiContractAPI) {
        this.huobiContractAPI = huobiContractAPI;
        actualHandleOrder();
    }

    void actualHandleOrder() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {

                //如果实际订单和虚拟订单都为null，直接结束当次循环
                if (actualRequestOrder == null && virtualRequestOrder == null) {
                    return;
                }
                if (virtualRequestOrder == null) {
                    if (!judgeCanOrderOrNot(actualRequestOrder)) {
                        return;
                    }
                    virtualRequestOrder = actualRequestOrder;
                    actualRequestOrder = null;
                    //第一次进行买单或卖单的处理，该函数根据买单或卖单，确定不同的挂单价
                    buySellRequestOrderHandle();
                }

                //查询上一次订单的成交详情
                ContractOrderInfoRequest orderInfoRequest = new ContractOrderInfoRequest(orderID, "", virtualRequestOrder
                        .getSymbol());
                ContractOrderInfo orderDetail = huobiContractAPI.getContractOrderInfo(orderInfoRequest).get(0);

                print(orderDetail);
                //如果订单是完全成交状态，则此次交易完全结束，把虚拟订单置为null
                if (orderDetail.getStatus() == (OrderStatus.FILLED.getCode())) {
                    print("完全成交");
                    virtualRequestOrder = null;
                    //如果订单没有完全成交，先撤销订单
                } else if (orderDetail.getStatus() == (OrderStatus.PARTIAL_FILLED.getCode()) || orderDetail.getStatus() == (OrderStatus.SUBMITTED.getCode())) {
                    CancelOrderResp cancelOrderResp = huobiContractAPI.cancelOrder(orderInfoRequest);
                    if (cancelOrderResp.getSuccesses().equals(String.valueOf(orderID))) {
                        return;
                    }
                    print("完全成交");
                    //如果撤单失败，说明已经成交，则把虚拟订单置为null
                    virtualRequestOrder = null;
                    //如果订单状态为撤销或部分成交撤销
                } else if (orderDetail.getStatus() == (OrderStatus.CANCELED.getCode()) || orderDetail.getStatus() == (OrderStatus
                        .PARTIAL_CANCELED.getCode())) {
                    //根据订单实际成交的情况，更新virtualFilledOrder对象
                    long tradeVolume = orderDetail.getTrade_volume();
                    long volumeUnfilled = virtualRequestOrder.getVolume() - tradeVolume;
                    print("volumeUnfilled:" + volumeUnfilled);
                    virtualRequestOrder.setVolume(volumeUnfilled);
                    buySellRequestOrderHandle();
                }

            }
        }, 0, HANG_ORDER_INTERVAL);// 取消订单，重新挂单的间隔为2s
    }

    /**
     * @Description: 根据买卖不同，分别确定买单或卖单的挂单价
     */
    private void buySellRequestOrderHandle() {
        double hangPrice;
        //创建contractSymbol
        String contractSymbol = getContractSymbol(virtualRequestOrder.getSymbol(), virtualRequestOrder.getContract_type());
        if (virtualRequestOrder.getDirection().equals("buy")) {
            double highestBuyPrice = huobiContractAPI.getDepth(contractSymbol, MergeLevel.STEP0).getBids().get(0).get
                    (0);
            //挂单价比最高买价，高0.01
            hangPrice = highestBuyPrice + 0.01;
        } else {
            double lowestSellPrice = huobiContractAPI.getDepth(contractSymbol, MergeLevel
                    .STEP0).getAsks().get(0).get(0);
            //挂单价比最低卖价，低0.01
            hangPrice = lowestSellPrice - 0.01;
        }
        virtualRequestOrder.setPrice(hangPrice);
        print(virtualRequestOrder);
        orderID = huobiContractAPI.placeOrder(virtualRequestOrder);
    }

    private boolean judgeCanOrderOrNot(ContractOrderRequest actualRequestOrder) {
        String needCloseSymbol = actualRequestOrder.getSymbol();
        long needCloseVolume = actualRequestOrder.getVolume();
        String needCloseDirection = actualRequestOrder.getDirection();
        List<ContractPositionInfo> contractPositionInfoList = huobiContractAPI.getContractPositionInfos();
        for (ContractPositionInfo contractPositionInfo : contractPositionInfoList) {
            if (contractPositionInfo.getSymbol().equals(needCloseSymbol) && (!contractPositionInfo.getDirection().equals(needCloseSymbol)) && contractPositionInfo.getAvailable() >= needCloseVolume) {
                return true;
            }
        }
        return false;
    }

}
