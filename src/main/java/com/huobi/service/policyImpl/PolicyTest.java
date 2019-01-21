package com.huobi.service.policyImpl;

import com.huobi.domain.POJOs.ContractAccountInfo;
import com.huobi.domain.POJOs.ContractOrderInfo;
import com.huobi.domain.POJOs.ContractPositionInfo;

import java.util.Timer;
import java.util.TimerTask;

import static com.huobi.constant.TradeConditionConsts.HANG_ORDER_INTERVAL;

/**
 * @Description
 * @Author squirrel
 * @Date 19-1-21 下午3:34
 */
public class PolicyTest {
    private ContractAccountInfo virtualAccountInfo;
    private ContractPositionInfo virtualPositionInfo;

    void autoTrade() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                openPosition();
                closePosition(virtualPositionInfo);
            }
        }, 0, HANG_ORDER_INTERVAL);// 取消订单，重新挂单的间隔为2s
    }

    private void openPosition() {
        if (virtualPositionInfo.getVolume()>0){
            return;
        }


    }

    private void closePosition(ContractPositionInfo virtualPositionInfo) {

    }


}
