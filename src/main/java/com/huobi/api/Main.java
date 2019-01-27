package com.huobi.api;


import com.huobi.service.*;
import com.huobi.service.policyImpl.PolicyWave;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.huobi.utils.PrintUtil.print;


public class Main {
    public static void main(String[] args) {

        InitSystem initSystem = new InitSystem();
        PolicyWave policyByMA = new PolicyWave(initSystem, true);
        policyByMA.autoTrade();


      /*  DisplaySystem displaySystem = new DisplaySystem(initSystem);
        System.out.println(displaySystem.functionDisplay);

        Scanner sc = new Scanner(System.in);
        while (true) {
            String cmd = sc.next();
            switch (cmd) {
                case "0":
                    System.out.println(displaySystem.functionDisplay);
                    break;
                case "1":
                    System.out.println(displaySystem.displayAccountInformation("BTC", "margin_balance"));
                    break;
                case "2":
                    System.out.println(displaySystem.displayAccountInformation("BTC", "margin_position"));
                    break;
                case "3":
                    System.out.println(displaySystem.queryPositionProfitRate("buy"));
                    System.out.println(displaySystem.queryPositionProfitRate("sell"));
                    break;
                case "q":
                    System.exit(0);
                    break;
                default:
                    System.out.println("错误指令");
                    break;
            }

        }



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

        print(MA5RateList);*/
    }


}


