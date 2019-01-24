package com.huobi.service.policyImpl;

import com.huobi.domain.POJOs.Kline;
import com.huobi.domain.enums.Resolution;
import com.huobi.utils.DateUtil;
import com.huobi.utils.ExcelHelper;
import com.huobi.utils.JxlExcelHelper;

import java.math.BigDecimal;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

public class MA {

    public static void addMAToKlineList(List<Kline> klineList,int averageCount) {
        //添加MA5
        for (int j = klineList.size(); j >= averageCount; j--) {
            double MA5Total = 0;
            for (int i = j - 1; i >= j - averageCount; i--) {
                MA5Total = MA5Total + klineList.get(i).getClose();
            }
            klineList.get(j - 1).setMA(MA5Total / averageCount);

        }
        //添加MA5Derivative
        for (int i = klineList.size(); i >= 2; i--) {
           double MA5Derivative=klineList.get(i - 1).getMA() - klineList.get(i - 2).getMA();
            BigDecimal temp=new BigDecimal(MA5Derivative);
            MA5Derivative=temp.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            klineList.get(i - 1).setMA_Derivative(MA5Derivative);

            double closePriceDerivative=klineList.get(i - 1).getClose() - klineList.get(i - 2).getClose();
            BigDecimal temp1=new BigDecimal(closePriceDerivative);
            closePriceDerivative=temp1.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            klineList.get(i - 1).setClosePriceDerivative(closePriceDerivative);

        }

    }

    public static void writeToExcel(List<Kline> klineList,int averageCount) {
        addMAToKlineList(klineList,averageCount);
        klineListAddRealTime(klineList);

        String[] fieldNames = {"realtime", "MA_Derivative","closePriceDerivative","close"};
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
