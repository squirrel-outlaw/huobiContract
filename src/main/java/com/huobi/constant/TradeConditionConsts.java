package com.huobi.constant;

/**
 * @Description 交易条件算法中的一些常数
 * @Author squirrel
 * @Date 18-9-20 上午11:05
 */
public interface TradeConditionConsts {

    int REQUEST_INTERVAL_LONG = 100;    //完全避免火币服务器报错，对火币服务器发送请求的间隔毫秒数
    int REALTIME_PRICE_LIST_FIXED_LENGTH = 5;     // 实时价格列表保存的实时价格的数量
    int HANG_ORDER_INTERVAL = 1 * 1000;   //取消订单，重新挂单间隔时间，单位ms

    int SAMPLING_INTERVAL = 3;   //自动更新DataManager中数据的间隔，单位秒
    int SAMPLING_INTERVAL_15S = 15;   //自动更新DataManager中数据的间隔，单位秒
    int SAMPLING_COUNTS = 3;     //取采样个数的平均值

    double MA_DERIVATIVE_BORDER_LONG_BIG = 1.5;     //根据MA导数的大小判断开多的上边界
    double MA_DERIVATIVE_BORDER_LONG_SMALL = 0.5;   //根据MA导数的大小判断开多的下边界
    double MA_DERIVATIVE_BORDER_SHORT_SMALL = -0.5;  //根据MA导数的大小判断开空的下边界
    double MA_DERIVATIVE_BORDER_SHORT_BIG = -1.5;    //根据MA导数的大小判断开空的上边界

    double MAX_OPEN_POSITION_PERCENT = 0.03;


    //PolicyWave用到的常数
    int GET_1HOUR_KLINE_COUNTS = 15 * 24;  //获取1小时K线的总个数

    double OPEN_POSITION_HANG_PRICE_ADJUST_RATE = 1.5 * 0.01; //开仓时挂的价格跟现价的差值比例
    double OPEN_POSITION_HANG_PRICE_SMALL_ADJUST_RATE = 0.1 * 0.01; //为了更容易成交，对开仓挂价做小幅度修正

    double CLOSE_POSITION_HANG_PRICE_RATE_MAX = 2 * 0.01;      //平仓挂单，价格最大幅度
    double CLOSE_POSITION_HANG_PRICE_RATE_MIDDLE = 1.5 * 0.01; //平仓挂单，价格中等幅度

    double THROUGH_GREEN_BIG_KLINE_RATE = 0.1 * 0.01;//穿过大阳线线最高价或最低价的幅度，导致目标买卖转向
    double RETURN_GREEN_BIG_KLINE_RATE = -0.1 * 0.01;
    double THROUGH_RED_BIG_KLINE_RATE = -0.1 * 0.01;//穿过大阴线最高价或最低价的幅度，导致目标买卖转向
    double RETURN_RED_BIG_KLINE_RATE = 0.1 * 0.01;

    double MARGIN_SAFE_RATE_INIT = 0.5;     //保证金初始安全比例

    double FORCE_CLOSE_POSITION_VOLUME_PERCENT_MIN = 0.3;    //当损失达到一定幅度(小）后,强行平仓的仓位百分比
    double MARGIN_SAFE_RATE_LOSS_MIN = 0.35;     //当损失达到一定幅度(小）后， 保证金安全比例

    double FORCE_CLOSE_POSITION_VOLUME_PERCENT_MIDDLE = 0.5;    //当损失达到一定幅度(中等）后,强行平仓的仓位百分比
    double MARGIN_SAFE_RATE_LOSS_MIDDLE = 0.25;     //当损失达到一定幅度(中等）后， 保证金安全比例

    double FORCE_CLOSE_POSITION_VOLUME_PERCENT_MAX = 1;    //当损失达到一定幅度(最大）后,强行平仓的仓位百分比
    double MARGIN_SAFE_RATE_LOSS_MAX = 0;     //当损失达到一定幅度(最大）后， 保证金安全比例

    long MARGIN_SAFE_RATE_SWITCH_TIME = 1 * 60 * 60 * 1000;  //保证金安全比例切换时的间隔

    long AFTER_FORCE_CLOSE_POSITION_WAIT_TIME = 10 * 60 * 60;  //当损失达到一定幅度后强行平仓以后，等待的时间
    long LATELY_BIG_KLINE_AFTER_FORCE_CLOSE_POSITION_WAIT_TIME = 10 * 60;  //当刚出现大K线后强行平仓，然后等待的时间

    int AUTO_TRADE_INTERVAL = 10 * 1000;   //自动交易的间隔时间，单位ms
    int RECORD_STATUS_SWITCH_TIME = 30;   //发生在此间隔内的多空状态转变，把它记录下来，单位秒

    //*********************************************************************************************************************
    //*********************************************************************************************************************
    //PolicyByLead用到的常数
    double POLICYBYLEAD_MARGIN_SAFE_RATE_INIT = 0.5;     //保证金初始安全比例, 越小，可用的越少

    double TODAY_KLINE_LENGTH_RATE_UP_LIMIT = 3.5;  //当天涨幅超过此值时不开仓
    double TODAY_KLINE_LENGTH_RATE_DOWN_LIMIT = -3.5;  //当天跌幅超过此值时不开仓

    double OPEN_POSITION_RATE_SMALL = 0.6 * 0.01;
    double OPEN_POSITION_RATE_MIDDLE = 0.85 * 0.01;
    double OPEN_POSITION_RATE_BIG = 1.1 * 0.01;

    double OPEN_POSITION_RATE_TODAY_BIG_KLINE_ADJUST = 2 * 0.01;
    double OPEN_POSITION_RATE_LAST_HOUR_BIG_KLINE_ADJUST = 2 * 0.01;
    double OPEN_POSITION_RATE_CAREFUL_LONG_ADJUST = 0.5 * 0.01;
    double OPEN_POSITION_RATE_CAREFUL_SHORT_ADJUST = 0.5 * 0.01;

    double TAKE_PROFIT_RATE_SMALL = 0.4 * 0.01;
    double TAKE_PROFIT_RATE_MIDDLE = 0.6 * 0.01;
    double TAKE_PROFIT_RATE_BIG = 0.8 * 0.01;
    double URGENT_CLOSE_POSITION_HANG_RATE_ADJUST = 0.2 * 0.01;
    double VERY_URGENT_CLOSE_POSITION_HANG_RATE_ADJUST = 0.3 * 0.01;

    int LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_SMALL = 15 * 60 * 1000;  //上个小时还有持仓没有平，对平仓价格进行修改的最小时间限制
    int LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_MIDDLE = 30 * 60 * 1000;
    int LAST_HOUR_POSITION_CLOSE_PRICE_ADJUST_TIME_INTERVAL_BIG = 45 * 60 * 1000;
    int WHOLE_TIME_STATUS_SWITCH_CONTINUED = 20 * 1000; //整点时，系统状态切换的持续时间

    int POLICY_BYLEAD_AUTO_TRADE_INTERVAL = 5 * 1000;   //自动交易的间隔时间，单位ms

    double FORCE_CLOSE_POSITION_LOSS_RATE_MIN = -0.25;         //当损失达到一定幅度(小）后强行平仓
    double FORCE_CLOSE_POSITION_LOSS_RATE_MIDDLE = -0.4;    //当损失达到一定幅度(中等）后强行平仓的阈值
    double FORCE_CLOSE_POSITION_LOSS_RATE_MAX = -0.5;        //当损失达到一定幅度(最大）后强行平仓的阈值


}
