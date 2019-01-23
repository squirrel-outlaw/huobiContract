package com.huobi.service.policyImpl;

import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.enums.Resolution;
import com.huobi.utils.DateUtil;
import com.huobi.utils.ExcelHelper;
import com.huobi.utils.JxlExcelHelper;

import java.util.List;

public class MA {

    public static void addMAToKlineList(List<Kline> klineList) {
        //生成MA5List
        for (int j = klineList.size(); j >= 5; j--) {
            double MA5Total = 0;
            for (int i = j - 1; i >= j - 5; i--) {
                MA5Total = MA5Total + klineList.get(i).getClose();
            }
            klineList.get(j - 1).setMA(MA5Total / 5);

        }
        //生成MA5DerivativeList
        for (int i = klineList.size(); i >= 2; i--) {
            klineList.get(i - 1).setMA_Derivative(klineList.get(i - 1).getMA() - klineList.get(i - 2).getMA());
        }

    }

    public static void writeToExcel(List<Kline> klineList) {
        addMAToKlineList(klineList);
        klineListAddRealTime(klineList);

        String[] fieldNames = {"realtime", "MA_Derivative"};
        ExcelHelper jxlExcelHelper = JxlExcelHelper.getInstance("/usr/local/huobi/temp/data/" + "MA_Derivative" + ".xls");
        try {
            jxlExcelHelper.writeExcel(Kline.class, klineList, fieldNames);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void klineListAddRealTime(List<Kline> klines) {
        for (int i = 0; i < klines.size(); i++) {
            Kline kline = klines.get(i);
            String realTime;
            realTime = DateUtil.format(kline.getId(), DateUtil.PATTERN_GRACE_NORMAL);
            kline.setRealtime(realTime);
            klines.set(i, kline);
        }
    }


}
