package com.huobi.constant;

/**
 * @Description 交易条件算法中的一些常数
 * @Author squirrel
 * @Date 18-9-20 上午11:05
 */
public interface TradeConditionConsts {

    String TRADE_ACCOUNT_DATA_PATH_1 = "/res/testAccount1.txt";  //保存测试账户资产数据的文本文件，相对于jar包位置的路径


    int REQUEST_INTERVAL_LONG = 100;    //完全避免火币服务器报错，对火币服务器发送请求的间隔毫秒数

    int REALTIME_PRICE_LIST_FIXED_LENGTH = 5;     // 实时价格列表保存的实时价格的数量

    int HANG_ORDER_INTERVAL = 1 * 1000;   //取消订单，重新挂单间隔时间，单位ms


    int SAMPLING_INTERVAL = 3;   //自动更新DataManager中数据的间隔，单位秒
    int SAMPLING_INTERVAL_15S = 15;   //自动更新DataManager中数据的间隔，单位秒
    int SAMPLING_COUNTS = 3;     //取采样个数的平均值

    double OPEN_LONG_POSITION_RATE_DERIVATIVE = 0.1;  //此阈值为交易对涨跌幅波动的导数，当超过此阈值时，开多
    double OPEN_SHORT_POSITION_RATE_DERIVATIVE = -0.1;  //开空
    double OPEN_POSITION_AVAILABLE_MARGIN_PERCENT = 95; //此阈值为开仓时可用保证金占总权益的百分比，当大于此阈值时才可开仓

    double CLOSE_LONG_POSITION_RATE_DERIVATIVE_LIMIT = -0.02;   //平多时，涨跌幅的变化率必须小于此值才进行，即必须跌了0.02%以上才平仓
    double CLOSE_SHORT_POSITION_RATE_DERIVATIVE_LIMIT = 0.02;

    double TAKE_PROFIT_RATE = 0.2;  //止盈比例,1代表百分之1
    double STOP_LOSS_RATE = -0.2;    //止损比例

    double MA_DERIVATIVE_BORDER_LONG_BIG = 1.5;     //根据MA导数的大小判断开多的上边界
    double MA_DERIVATIVE_BORDER_LONG_SMALL = 0.5;   //根据MA导数的大小判断开多的下边界
    double MA_DERIVATIVE_BORDER_SHORT_SMALL = -0.5;  //根据MA导数的大小判断开空的下边界
    double MA_DERIVATIVE_BORDER_SHORT_BIG = -1.5;    //根据MA导数的大小判断开空的上边界

    double MAX_OPEN_POSITION_PERCENT = 0.03;

    //PolicyWave用到的常数
    int GET_1HOUR_KLINE_COUNTS = 15 * 24;  //获取1小时K线的总个数

    double OPEN_POSITION_HANG_PRICE_ADJUST_RATE = 1.5 * 0.01; //开仓时挂的价格跟现价的差值比例
    double OPEN_POSITION_HANG_PRICE_SMALL_ADJUST_RATE = 0.1 * 0.01; //为了更容易成交，对开仓挂价做小幅度修正

    double MARGIN_SAFE_PERCENT = 0.5;  //开仓时的安全比例
    double CLOSE_POSITION_HANG_PRICE_RATE_MAX = 2 * 0.01;      //平仓挂单，价格最大幅度
    double CLOSE_POSITION_HANG_PRICE_RATE_MIDDLE = 1.5 * 0.01; //平仓挂单，价格中等幅度

    double THROUGH_GREEN_BIG_KLINE_RATE = 0.1 * 0.01;//穿过大阳线线最高价或最低价的幅度，导致目标买卖转向
    double RETURN_GREEN_BIG_KLINE_RATE = -0.1 * 0.01;
    double THROUGH_RED_BIG_KLINE_RATE = -0.1 * 0.01;//穿过大阴线最高价或最低价的幅度，导致目标买卖转向
    double RETURN_RED_BIG_KLINE_RATE = 0.1 * 0.01;

    double FORCE_CLOSE_POSITION_LOSS_rate = -0.4;   //当损失达到一定幅度后强行平仓的阈值
    long AFTER_FORCE_CLOSE_POSITION_WAIT_TIME = 10 * 60 * 60;  //当损失达到一定幅度后强行平仓以后，等待的时间
    long LATELY_BIG_KLINE_AFTER_FORCE_CLOSE_POSITION_WAIT_TIME = 10 * 60 ;  //当刚出现大K线后强行平仓，然后等待的时间

    int AUTO_TRADE_INTERVAL = 10 * 1000;   //自动交易的间隔时间，单位ms

}
