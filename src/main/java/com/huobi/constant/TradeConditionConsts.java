package com.huobi.constant;

/**
 * @Description 交易条件算法中的一些常数
 * @Author squirrel
 * @Date 18-9-20 上午11:05
 */
public interface TradeConditionConsts {

    String TRADE_ACCOUNT_DATA_PATH_1 = "/res/testAccount1.txt";  //保存测试账户资产数据的文本文件，相对于jar包位置的路径
    String TRADE_ACCOUNT_INIT_BALANCE = "0.5";                       //测试账户初始化资产

    int REQUEST_INTERVAL = 50;          //对火币服务器发送请求的间隔毫秒数
    int REQUEST_INTERVAL_LONG = 100;    //完全避免火币服务器报错，对火币服务器发送请求的间隔毫秒数

    int REALTIME_PRICE_LIST_FIXED_LENGTH = 5;     // 实时价格列表保存的实时价格的数量
    int REALTIME_PRICE_LIST_SAMPLED_PERIOD = 5;  //处理实时价格列表时的取样周期
    int REALTIME_PRICE_LIST_SAMPLED_ACCURACY = 3;  //处理实时价格列表时的取样精度（即一共取几个值的平均值）

    double BUY_TRADE_POINT = 2;       //买入的判断阈值，当评价分大于此值时，判断可以买入
    double REALTIME_PRICE_DER_COEF_Last = 1;     //计算tradePoint时，最后一个REALTIME_PRICE_DER的修正系数
    double REALTIME_PRICE_DER_COEF_2RDLAST = 0;  //计算tradePoint时，倒数第二个REALTIME_PRICE_DER的修正系数
    double REALTIME_PRICE_DER_COEF_3THLAST = 0;  //计算tradePoint时，倒数第三个REALTIME_PRICE_DER的修正系数
    double POSITION_RATE_COEFF = 1.5;    //计算tradePoint时,头寸仓位对tradePoint的影响的修正系数

    int HANG_ORDER_INTERVAL = 2 * 1000;   //取消订单，重新挂单间隔时间，单位ms
    double HANG_ORDER_ADJUST_RATE = 0.0005; //对挂单价格进行修正的比率
    double CLOSING_ORDER_PROFIT_RATE = 0.02;  //结束订单所需要的盈利比例（即达到这个比例时结束订单）
}
