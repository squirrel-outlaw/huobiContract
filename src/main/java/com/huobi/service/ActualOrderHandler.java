package com.huobi.service;

import com.huobi.api.HuobiContractAPI;
import com.huobi.domain.POJOs.ContractOrderInfo;
import com.huobi.domain.enums.MergeLevel;
import com.huobi.domain.request.ContractOrderInfoRequest;
import com.huobi.domain.request.ContractOrderRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.huobi.constant.TradeConditionConsts.HANG_ORDER_ADJUST_RATE;
import static com.huobi.constant.TradeConditionConsts.HANG_ORDER_INTERVAL;
import static com.huobi.utils.PrintUtil.print;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-16 下午4:06
 */


public class ActualOrderHandler {


    //借用Order对象的属性，虚拟的完成订单对象，用来保存最终完成交易的详情
    //借用amount，price，filledCashAmount这三个属性值，其中filledCashAmount是成交计价货币使用量，包含手续费
    private Order virtualFilledOrder;
    private List<Order> virtualFilledOrderList;

    private HuobiContractAPI huobiContractAPI;
    private TradeSystem tradeSystem;
    private Account virtualTradeAccount;             //虚拟交易账户
    private List<Account> virtualTradeAccountList;  //虚拟交易账户列表

    private ContractOrderRequest virtualRequestOrder;    //借用Order对象的属性，虚拟的请求订单对象，此虚拟对象包含symbol,orderMode, orderType,amount等属性值
    private PlacingOrderMode orderMode;
    private long orderID;

    ActualOrderHandler(TradeSystem tradeSystem, List<Account> virtualTradeAccountList, Order virtualRequestOrder, PlacingOrderMode orderMode) {
        this.tradeSystem = tradeSystem;
        this.virtualFilledOrderList = tradeSystem.virtualFilledOrderList;
        this.huobiContractAPI = tradeSystem.huobiContractAPI;
        this.virtualTradeAccount = virtualTradeAccountList.get(0);
        this.virtualTradeAccountList = virtualTradeAccountList;
        this.virtualRequestOrder = virtualRequestOrder;
        this.orderMode = orderMode;
        //初始化virtualFilledOrder
        virtualFilledOrder = new Order();
        virtualFilledOrder.setSymbol(virtualRequestOrder.getSymbol());
        virtualFilledOrder.setType(virtualRequestOrder.getType());
        virtualFilledOrder.setAmount(BigDecimal.valueOf(0));
        virtualFilledOrder.setFilledCashAmount(BigDecimal.valueOf(0));
    }

    void actualHandleOrder() {
        //第一次进行买单或卖单的处理，该函数根据买单或卖单，确定不同的挂单价
        buySellRequestOrderHandle();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                //查询上一次订单的成交详情
                ContractOrderInfoRequest orderInfoRequest = new ContractOrderInfoRequest(orderID, "", virtualRequestOrder
                        .getSymbol());
                ContractOrderInfo orderDetail = huobiContractAPI.getContractOrderInfo(orderInfoRequest);

                //如果订单是完全成交状态，更新virtualFilledOrder，终止timer
                if (orderDetail.getState().equals(OrderState.FILLED)) {
                    //根据订单实际成交的情况，更新virtualFilledOrder对象
                    updateFilledOrder(orderDetail);
                    //计算平均成交价格，写入virtualFilledOrder
                    double price = virtualFilledOrder.getFilledCashAmount().doubleValue() / virtualFilledOrder
                            .getAmount().doubleValue();
                    virtualFilledOrder.setPrice(BigDecimal.valueOf(price));
                    //更新虚拟交易账户的相关信息
                    TradeSystem.updateTradeAccount(virtualFilledOrder, virtualTradeAccount, virtualTradeAccountList);
                    //把已完成的交易放入virtualFilledOrder列表
                    virtualFilledOrderList.add(virtualFilledOrder);
                    //终止定时器
                    this.cancel();

                    //如果订单没有完全成交，先撤销订单
                } else if (orderDetail.getState().equals(OrderState.PARTIAL_FILLED) || orderDetail.getState().equals(OrderState.SUBMITTED)) {
                    //有可能出现前面还是SUBMITTED状态，可是实际已经FILLED，导致撤单报错
                    try {
                        huobiApiRestClient.cancelOrder(orderID);
                    } catch (IllegalStateException e) {
                    }
                    //如果订单状态为撤销或部分成交撤销
                } else if (orderDetail.getState().equals(OrderState.CANCELED) || orderDetail.getState().equals(OrderState
                        .PARTIAL_CANCELED)) {
                    //根据订单实际成交的情况，更新virtualFilledOrder对象
                    updateFilledOrder(orderDetail);
                    //计算还需要继续完成成交的量
                    double amountUnfilled = virtualRequestOrder.getAmount().doubleValue() - orderDetail.getFieldAmount().doubleValue();

                    print("amountUnfilled:" + amountUnfilled);

                    virtualRequestOrder.setAmount(BigDecimal.valueOf(amountUnfilled));
                    buySellRequestOrderHandle();
                }

            }
        }, HANG_ORDER_INTERVAL, HANG_ORDER_INTERVAL);// 取消订单，重新挂单的间隔为2s
    }

    /**
     * @Description: 根据买卖不同，分别确定买单或卖单的挂单价
     */
    private void buySellRequestOrderHandle() {
        double hangPrice;
        if (virtualRequestOrder.getDirection().equals("buy")) {
            double highestBuyPrice = huobiContractAPI.getDepth(virtualRequestOrder.getSymbol(), MergeLevel.STEP0).getBids().get(0).get
                    (0);
            //挂单价比最高买价，高0.01
            hangPrice = highestBuyPrice + 0.01;
        } else {
            double lowestSellPrice = huobiContractAPI.getDepth(virtualRequestOrder.getSymbol(), MergeLevel
                    .STEP0).getAsks().get(0).get(0);
            //挂单价比最低卖价，低0.01
            hangPrice = lowestSellPrice - 0.01;
        }
        virtualRequestOrder.setPrice(hangPrice);
        print(virtualRequestOrder);
        orderID = huobiContractAPI.placeOrder(virtualRequestOrder);
    }

    /**
     * @Description: 根据订单实际成交的情况，更新virtualFilledOrder对象
     * @param: orderDetail：订单实际成交的情况
     */
    private void updateFilledOrder(Order orderDetail) {
        //如果是买单，手续费在基础货币中扣除
        if (virtualRequestOrder.getType().getCode().startsWith("buy")) {
            //买单的手续费为基础币，更新virtualFilledOrder的amount为之前保存的和这一次成交之和 ，再减去手续费
            double amount = virtualFilledOrder.getAmount().doubleValue() + orderDetail.getFieldAmount()
                    .doubleValue() - orderDetail.getFieldFees().doubleValue();
            virtualFilledOrder.setAmount(BigDecimal.valueOf(amount));
            //更新virtualFilledOrder的filledCashAmount(花费的计价货币量）为上一次的与这一次的和
            double filledCashAmount = virtualFilledOrder.getFilledCashAmount().doubleValue() + orderDetail
                    .getFieldCashAmount().doubleValue();
            virtualFilledOrder.setFilledCashAmount(BigDecimal.valueOf(filledCashAmount));
            //如果是卖单，手续费在计价货币中扣除
        } else {
            double amount = virtualFilledOrder.getAmount().doubleValue() + orderDetail.getFieldAmount()
                    .doubleValue();
            virtualFilledOrder.setAmount(BigDecimal.valueOf(amount));
            double filledCashAmount = virtualFilledOrder.getFilledCashAmount().doubleValue() + orderDetail
                    .getFieldCashAmount().doubleValue() - orderDetail.getFieldFees().doubleValue();
            virtualFilledOrder.setFilledCashAmount(BigDecimal.valueOf(filledCashAmount));
        }
    }


}
