package com.huobi.service;


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

import static com.huobi.utils.PrintUtil.print;

/**
 * @Author: squirrel
 * @Date: 18-9-19 上午9:11
 */
public class TradeSystem {
    public void autoTrade(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {

            }
        }, HANG_ORDER_INTERVAL, HANG_ORDER_INTERVAL);// 取消订单，重新挂单的间隔为2s
    }

}

