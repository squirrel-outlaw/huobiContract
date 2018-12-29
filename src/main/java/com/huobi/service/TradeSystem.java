package com.huobi.service;

import com.huobi.api.HuobiApiRestClient;
import com.huobi.domain.POJOs.Account;
import com.huobi.domain.POJOs.Asset;
import com.huobi.domain.POJOs.Depth;
import com.huobi.domain.POJOs.Order;
import com.huobi.domain.POJOs.customerPOJOs.TradeResult;
import com.huobi.domain.POJOs.customerPOJOs.TradeSignal;
import com.huobi.domain.enums.*;
import com.huobi.utils.jsonSerilizableUtils.JsonSerializable;
import com.huobi.utils.jsonSerilizableUtils.ReadAndWriteJson;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.huobi.constant.TradeConditionConsts.*;
import static com.huobi.service.BasicFunction.convertBalanceToUSDT;
import static com.huobi.service.BasicFunction.getCurrencyFromSymbol;
import static com.huobi.utils.PrintUtil.print;

/**
 * @Author: squirrel
 * @Date: 18-9-19 上午9:11
 */
public class TradeSystem {
    Map<String, Integer[]> USDTSymbols;
    HuobiApiRestClient huobiApiRestClient;
    private Map<AccountType, Object> accountsID;
    //交易账户资产信息列表，交易账户是隔离出来的虚拟账户
    public List<Account> tradeAccountTestList;
    //虚拟订单列表，把每一笔买入的订单放入其中。每一个虚拟订单包含数量，平均价格（已算入手续费）
    public List<Order> virtualFilledOrderList;


    //保存每一笔交易结果的列表
    public List<TradeResult> tradeResultList = new ArrayList<>();

    public TradeSystem(InitSystem initSystem) {
        this.USDTSymbols = initSystem.USDTSymbols;
        this.huobiApiRestClient = initSystem.huobiApiRestClient;
        this.accountsID = initSystem.accountsID;
        this.tradeAccountTestList = initSystem.tradeAccountTestList;
    }


    public void autoTradeByPolicy(Policy policy) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                policy.closingVirtualFilledOrder(virtualFilledOrderList, TradeSystem.this);

                //List<TradeSignal> tradeSignalList = policy.generateTradeSignalList();
                List<TradeSignal> tradeSignalList=new ArrayList<>();
                double[] availableTradeBalanceArray={0,0,0};
                // double[] availableTradeBalanceArray = calcAvailableTradeBalance(tradeAccountTestList, USDTSymbols);
               // print(availableTradeBalanceArray);

                if (availableTradeBalanceArray[2] < 0.1) {
                    return;
                }
                //当tradeSignalList为空时，直接返回
                if (null == tradeSignalList) {
                    return;
                }

                for (TradeSignal tradeSignal : tradeSignalList) {
                    String signalSymbol = tradeSignal.getSymbol();
                    double signalSymbolPrice = huobiApiRestClient.getTrade(signalSymbol).getPrice();
                    double amount = availableTradeBalanceArray[1] * 0.5 / signalSymbolPrice;
                    //新建一个virtualRequestOrder
                    Order virtualRequestOrder = new Order();
                    virtualRequestOrder.setSymbol(signalSymbol);
                    virtualRequestOrder.setAmount(BigDecimal.valueOf(amount));
                    virtualRequestOrder.setType(OrderType.BUY_LIMIT);
                    tradeHandling(virtualRequestOrder, PlacingOrderMode.buySpot, tradeAccountTestList);
                }
                //TradeSignal tradeSignal = Collections.max(policyManger.buyTradeSignalList);
            }
        }, HANG_ORDER_INTERVAL, HANG_ORDER_INTERVAL);// 取消订单，重新挂单的间隔为2s

    }


    /**
     * @Description: 根据交易对和买卖方式来进行下单
     * @param: order: 借用Order对象的属性(symbol,price,amount,orderType)，构建一个请求订单对象
     * @return: 返回订单号
     */
    public long placingOrder(Order order, PlacingOrderMode orderMode) {

        String symbol = order.getSymbol();
        double amount = order.getAmount().doubleValue();
        double price = order.getPrice().doubleValue();
        OrderType orderType = order.getType();

        //根据要求的买卖方式求取账户ID
        String accountID = null;
        OrderSource orderSource = null;
        switch (orderMode) {
            case buySpot:
            case sellSpot:
                //普通交易时，accountID为spotID
                accountID = (String) accountsID.get(AccountType.spot);
                //普通交易时，OrderSource为SPOT_API
                orderSource = OrderSource.SPOT_API;
                break;
            case buyMargin:
            case sellMargin:
                //杠杆交易时，OrderSource为MARGIN_API
                orderSource = OrderSource.MARGIN_API;
                Map<String, String> marginAccountIDsMap = (Map<String, String>) accountsID.get(AccountType.margin);
                //如果accountsID Map里面包含该交易对，则取出 marginAccountID
                if (marginAccountIDsMap.containsKey(symbol)) {
                    accountID = marginAccountIDsMap.get(symbol);
                    break;
                } else {
                    //如果accountsID Map里面没有该交易对，直接返回
                    return -1;
                }
        }
        //交易对的交易价格和数量的精度位数，第一个存放价格精度，第二个存放数量精度
        Integer[] precisions = USDTSymbols.get(symbol);
        String priceFormatted = String.format("%." + precisions[0] + "f", price);
        String amountFormatted = String.format("%." + precisions[1] + "f", amount);
        print(amountFormatted);
        return huobiApiRestClient.placeOrder(accountID, amountFormatted, priceFormatted, orderSource,
                symbol, orderType);
    }

    /**
     * @Description: 处理买卖订单的中间函数，调用ActualOrderHandler对象完成实际的订单处理
     * @param: virtualRequestOrder：借用Order对象的属性，构建一个虚拟的请求订单对象，此虚拟对象包含symbol,
     * orderMode, orderType,amount等属性值
     * @return:
     */
    public void tradeHandling(Order virtualRequestOrder, PlacingOrderMode
            orderMode, List<Account> virtualTradeAccountList) {
        ActualOrderHandler actualOrderHandler = new ActualOrderHandler(this, virtualTradeAccountList, virtualRequestOrder, orderMode);
        actualOrderHandler.actualHandleOrder();
    }

    /**
     * @Description: 计算虚拟交易账户资产分布情况
     * @param: tradeAccountList: 虚拟交易账户列表
     * @return: double[]：数组第一项为总资产，第二项为可用资产，第三项为可用资产百分比
     */
    public Map<String, Object> calcAvailableTradeBalance(List<Account> tradeAccountList, Map<String, Integer[]>
            USDTSymbols) {
        Map<String, Object> virtualAccountInformationMap = new HashMap<>();

        double[] aviableTradeBalanceArray = new double[3];

        Map<String, Double[]> currencyPercentMap = new HashMap<>();
        double availableUsdt = 0d; //可用的usdt资产
        double others = 0d; //其他的币种资产
        for (Account account : tradeAccountList) {
            //从虚拟交易账户列表中找出spot账户
            if (account.getType().equals(AccountType.spot)) {
                //从spot账户的Asset列表找出币种为usdt的Asset
                for (Asset asset : account.getList()) {
                    if (asset.getCurrency().equals("usdt")) {
                        availableUsdt = asset.getBalance().doubleValue();
                    } else {
                        String currency = asset.getCurrency();
                        double balance = asset.getBalance().doubleValue();
                        double balanceUsdt = convertBalanceToUSDT(balance, currency, huobiApiRestClient, USDTSymbols);
                        others = others + balanceUsdt;
                        Double[] currencyPercent = new Double[2];
                        currencyPercent[0] = balanceUsdt;
                        currencyPercentMap.put(currency, currencyPercent);
                    }
                }
                aviableTradeBalanceArray[0] = availableUsdt + others;
                aviableTradeBalanceArray[1] = availableUsdt;
                aviableTradeBalanceArray[2] = aviableTradeBalanceArray[1] / aviableTradeBalanceArray[0];

                for (Map.Entry<String, Double[]> entry : currencyPercentMap.entrySet()) {
                    entry.getValue()[1] = entry.getValue()[0] / aviableTradeBalanceArray[0];
                }
                virtualAccountInformationMap.put("currencyPercentMap", currencyPercentMap);
                virtualAccountInformationMap.put("avai  lableTradeBalance", aviableTradeBalanceArray);
            }
        }
        return virtualAccountInformationMap;
    }

    public static void updateTradeAccount(Order virtualFilledOrder, Account virtualTradeAccount, List<Account>
            virtualTradeAccountList) {
        //取得基础币种
        String currency = getCurrencyFromSymbol(virtualFilledOrder.getSymbol());
        //虚拟交易账户里是否存在该币种
        Boolean isCurrencyExisted = false;
        for (Asset asset : virtualTradeAccount.getList()) {
            //先更新计价币种usdt的值
            if (asset.getCurrency().equals("usdt")) {
                //filledOrder成交的usdt的值
                double orderUsdt = virtualFilledOrder.getFilledCashAmount().doubleValue();
                //如果filledOrder是买单
                if (virtualFilledOrder.getType().getCode().startsWith("buy")) {
                    double balance = asset.getBalance().doubleValue() - orderUsdt;
                    //更新asset里的usdt的值
                    asset.setBalance(BigDecimal.valueOf(balance));
                } else {
                    //否则，如果是卖单的话
                    double balance = asset.getBalance().doubleValue() + orderUsdt;
                    asset.setBalance(BigDecimal.valueOf(balance));
                }
            }
            //再更新基础币种的值
            if (asset.getCurrency().equals(currency)) {
                //asset列表里存在该币种
                isCurrencyExisted = true;
                if (virtualFilledOrder.getType().getCode().startsWith("buy")) {
                    //把已完成订单里的amount加入到asset里的相关币种的balance里
                    double balance = asset.getBalance().doubleValue() + virtualFilledOrder.getAmount().doubleValue();
                    asset.setBalance(BigDecimal.valueOf(balance));
                } else {
                    double balance = asset.getBalance().doubleValue() - virtualFilledOrder.getAmount().doubleValue();
                    asset.setBalance(BigDecimal.valueOf(balance));
                }
            }
        }
        //如果asset列表里没有该基础币种的信息,那么一定是买入
        if (!isCurrencyExisted) {
            Asset asset = new Asset();
            asset.setCurrency(currency);
            asset.setBalance(virtualFilledOrder.getAmount());
            virtualTradeAccount.getList().add(asset);
        }
        //把变动后的虚拟账户列表保存到本地
        String accountPath = ReadAndWriteJson.getCurrentDirPath() + TRADE_ACCOUNT_DATA_PATH_1;
        try {
            JsonSerializable.serializeToFile(virtualTradeAccountList, accountPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ActualOrderHandler {

    //借用Order对象的属性，虚拟的完成订单对象，用来保存最终完成交易的详情
    //借用amount，price，filledCashAmount这三个属性值，其中filledCashAmount是成交计价货币使用量，包含手续费
    private Order virtualFilledOrder;
    private List<Order> virtualFilledOrderList;

    private HuobiApiRestClient huobiApiRestClient;
    private TradeSystem tradeSystem;
    private Account virtualTradeAccount;             //虚拟交易账户
    private List<Account> virtualTradeAccountList;  //虚拟交易账户列表

    private Order virtualRequestOrder;    //借用Order对象的属性，虚拟的请求订单对象，此虚拟对象包含symbol,orderMode, orderType,amount等属性值
    private PlacingOrderMode orderMode;
    private long orderID;

    ActualOrderHandler(TradeSystem tradeSystem, List<Account> virtualTradeAccountList, Order virtualRequestOrder, PlacingOrderMode orderMode) {
        this.tradeSystem = tradeSystem;
        this.virtualFilledOrderList = tradeSystem.virtualFilledOrderList;
        this.huobiApiRestClient = tradeSystem.huobiApiRestClient;
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
                Order orderDetail = huobiApiRestClient.queryOrderDetail(orderID);

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
        if (virtualRequestOrder.getType().getCode().startsWith("buy")) {
            double highestBuyPrice = huobiApiRestClient.getDepth(virtualRequestOrder.getSymbol(), MergeLevel.STEP0).getBids().get(0).get
                    (0);
            //挂单价比最高买价，高出0.05%
            double hangPrice = highestBuyPrice * (1 + HANG_ORDER_ADJUST_RATE);
            virtualRequestOrder.setPrice(BigDecimal.valueOf(hangPrice));
            //如果是卖单,挂单价比最低卖单低百分之0.05
        } else {
            double lowestSellPrice = huobiApiRestClient.getDepth(virtualRequestOrder.getSymbol(), MergeLevel
                    .STEP0).getAsks().get(0).get(0);
            //挂单价比最低卖价，低出0.05%
            double hangPrice = lowestSellPrice * (1 - HANG_ORDER_ADJUST_RATE);
            virtualRequestOrder.setPrice(BigDecimal.valueOf(hangPrice));
        }
        print(virtualRequestOrder);
        orderID = tradeSystem.placingOrder(virtualRequestOrder, orderMode);
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

