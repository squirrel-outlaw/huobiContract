package com.huobi.api;

import com.huobi.domain.POJOs.ContractAccountInfo;
import com.huobi.domain.POJOs.ContractPositionInfo;
import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.enums.MergeLevel;
import com.huobi.domain.enums.Resolution;
import com.huobi.domain.request.ContractOrderInfoRequest;
import com.huobi.domain.request.ContractOrderRequest;
import com.huobi.domain.response.CancelOrderResp;
import com.huobi.service.*;
import com.huobi.service.policyImpl.MA;
import com.huobi.service.policyImpl.PolicyByMA;
import lombok.extern.slf4j.Slf4j;


import java.util.ArrayList;
import java.util.List;

import static com.huobi.utils.PrintUtil.print;


public class Main {
    public static void main(String[] args) {

        InitSystem initSystem = new InitSystem();
        // ActualOrderHandler actualOrderHandler = new ActualOrderHandler(initSystem.huobiContractAPI);
        // PolicyByMA policyByMA=new PolicyByMA(initSystem,actualOrderHandler);
        // policyByMA.autoTrade();
        List<Kline> klineList = initSystem.huobiContractAPI.getKlines("BTC_CQ", Resolution.M240, "2000");
        MA.writeToExcel(klineList,5);



      /*  List<Kline> klineList = initSystem.huobiContractAPI.getKlines("BTC_CQ", Resolution.M15, "100");
        List<Double> MA5List = new ArrayList<>();
        for (int j = klineList.size(); j >= 5; j--) {
            double MA5Total = 0;
            for (int i = j - 1; i >= j - 5; i--) {
                MA5Total = MA5Total + klineList.get(i).getClose();
            }
            MA5List.add(MA5Total / 5);
        }

        List<Double> MA5RateList = new ArrayList<>();

        for (int i = 0; i < MA5List.size() - 1; i++) {
            MA5RateList.add((double)Math.round((MA5List.get(i) - MA5List.get(i + 1)) * 100 )/ 100);
        }

        print(MA5RateList);










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

   /* public void commandHandle(int cmd) {
        switch (cmd) {
            case 1:

                break;
        }
    }*/


}


