package com.huobi.api;


import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.enums.Resolution;
import com.huobi.service.*;
import com.huobi.service.policyImpl.PolicyByLead;
import com.huobi.service.policyImpl.PolicyWave;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.huobi.constant.TradeConditionConsts.GET_1HOUR_KLINE_COUNTS;
import static com.huobi.utils.PrintUtil.print;

public class Main {
    public static void main(String[] args) {
        InitSystem initSystem = new InitSystem();
        Policy policyByLead = new PolicyByLead(initSystem, true);
        policyByLead.autoTrade();

        DisplaySystem displaySystem = new DisplaySystem(initSystem, policyByLead);
        displaySystem.displayFunction();
        Scanner sc = new Scanner(System.in);
        while (true) {
            String cmd = sc.next();
            switch (cmd) {
                case "0":
                    displaySystem.isDisplayPolicyRunningStatus = false;
                    displaySystem.displayFunction();
                    break;
                case "1":
                    displaySystem.isDisplayPolicyRunningStatus = false;
                    displaySystem.displayAccountInformation("BTC");
                    break;
                case "2":
                    displaySystem.isDisplayPolicyRunningStatus = true;
                    displaySystem.displayPolicyRunningStatus();
                    break;
                case "3":
                    displaySystem.isDisplayPolicyRunningStatus = false;
                    displaySystem.displayForceClosePositionInfo();
                    break;
                case "4":
                    displaySystem.isDisplayPolicyRunningStatus = false;
                    displaySystem.displayOpenPositionHangStatusInfo();
                    break;
                case "5":
                    displaySystem.isDisplayPolicyRunningStatus = false;
                    displaySystem.displayClosePositionHangStatusInfo();
                    break;
                case "q":
                    displaySystem.isDisplayPolicyRunningStatus = false;
                    System.exit(0);
                    break;
                default:
                    displaySystem.isDisplayPolicyRunningStatus = false;
                    System.out.println("错误指令!");
                    break;
            }
        }
    }
}


