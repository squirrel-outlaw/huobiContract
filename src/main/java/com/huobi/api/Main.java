package com.huobi.api;

import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.enums.Resolution;
import com.huobi.service.*;
import com.huobi.service.policyImpl.PolicyOne;
import com.huobi.service.policyImpl.PolicyTwo;
import com.huobi.utils.security.AESEncryption;

import java.util.List;

import static com.huobi.utils.PrintUtil.print;


public class Main {
    public static void main(String[] args) {
        InitSystem initSystem = new InitSystem();
        List<Kline> test=initSystem.huobiApiRestClient.getKlines("BTC_CW",Resolution.M5,"100");
        print(test);

        /*InitSystem initSystem = new InitSystem();
        DataManager dataManager = new DataManager(initSystem);

        TradeSystem tradeSystem = new TradeSystem(initSystem);
        Policy policy=new PolicyTwo(dataManager);
        tradeSystem.autoTradeByPolicy(policy);



       /*
        while (true) {
            try {
                Thread.sleep(3 * 1000);
            } catch (Exception e) {
            }
            print(dataManager.realTimePriceMap);
            print(dataManager.dataMaps5Seconds[0]);
            print(dataManager.dataMaps5Seconds[1]);
            print(dataManager.dataMaps5Seconds[2]);
        }







       /* Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n" +
                    "cmd:\n" +
                    "1.开仓前设置买家与卖家期望\n" +
                    "2.开仓撮合买家与卖家交易\n" +
                    "3.建仓查看保证金\n" +
                    "4.持仓查看保证金\n" +
                    "5.平仓撮合\n" +
                    "6.清算平仓时的差额收益\n" +
                    "7.退出\n");
            int cmd = sc.nextInt();
        }






       /* while (true) {


                // 查询的K线结果，在list里是按照时间上从最近到以前来保存
                // List<Kline> klines = client.kline("btcusdt", "15min", "10");
                // print(klines);
                //----查询所有账户状态 -------------------------------------------------------
                // print(client.accounts());
                //IntrustOrdersDetailRequest req = new IntrustOrdersDetailRequest();
                //IntrustDetailResponse intrustDetail = client.intrustOrdersDetail(req);
                // print(intrustDetail);
                // print(client.getAllOpenOrders(req));
            } catch (ApiException e) {
                System.err.println("API Error! err-code: " + e.getErrCode() + ", err-msg: " + e.getMessage());
                e.printStackTrace();
            }
        }*/
    }

    public void commandHandle(int cmd) {
        switch (cmd) {
            case 1:

                break;
        }
    }


}


