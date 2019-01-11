package com.huobi.constant;

/**
 * @Description 交易条件算法中的一些常数
 * @Author squirrel
 * @Date 18-9-20 上午11:05
 */
public interface TradeConditionConsts {

    String TRADE_ACCOUNT_DATA_PATH_1 = "/res/testAccount1.txt";  //保存测试账户资产数据的文本文件，相对于jar包位置的路径

    int REQUEST_INTERVAL = 50;          //对火币服务器发送请求的间隔毫秒数
    int REQUEST_INTERVAL_LONG = 100;    //完全避免火币服务器报错，对火币服务器发送请求的间隔毫秒数

    int REALTIME_PRICE_LIST_FIXED_LENGTH = 5;     // 实时价格列表保存的实时价格的数量

    int HANG_ORDER_INTERVAL = 2 * 1000;   //取消订单，重新挂单间隔时间，单位ms

    int AUTO_TRADE_INTERVAL=1*1000;   //自动交易的间隔时间，单位ms

    double TAKE_PROFIT_RATE=0.01;  //止盈比例
    double STOP_LOSS_RATE=-0.01;    //止损比例

}
