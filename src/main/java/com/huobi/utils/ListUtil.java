package com.huobi.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author squirrel
 * @Date 18-9-19 下午3:31
 */
public class ListUtil {
    /**
     * @Description: 取列表中固定数目的数据，去掉列表前面的数据
     * @param:
     * @return:
     */
    public static <T> void fixListLength(List<T> list, int length) {
        if (list.size() > length) {
            list.subList(list.size() - length, list.size());
        }
    }

    /**
     * @Description: 把列表按照周期samplingPeriod取样，取样方法是倒序的，从后往前，包含取样点的三个值的平均值作为取样结果，
     * 之后计算的每个取样结果放在列表的前一项，目的是最新的成交价一定会在取样列表里
     * @param:
     * @return:
     */
    public static List<Double> listSampleHandle(List<Double> list, int samplingPeriod, int averageCounts) {
        List<Double> result = new ArrayList<>();
        //如果原列表的项，大于等于取样周期，把这个判断放在最前面，目的是此判断为true的可能性占大多数
        if (list.size() >= samplingPeriod) {
            //当列表项除以取样周期的余数，小于取样精度-1（取样点也算上，所以减1），取样结果就减少一项
            int samplingCountAdjust = 0;
            if (list.size() % samplingPeriod < averageCounts) {
                samplingCountAdjust = 1;
            }

            //计算一共要获得取样结果的数目
            for (int i = 0; i <= list.size() / samplingPeriod - samplingCountAdjust; i++) {
                //从后往前以此得到每个取样结果
                double samplingSum = 0d;
                for (int j = list.size() - 1 - samplingPeriod * i; j > list.size() - 1 - samplingPeriod * i - averageCounts; j--) {
                    samplingSum = list.get(j) + samplingSum;
                }
                double samplingAverage = samplingSum / averageCounts;
                //把得到的结果插入到list的最开始索引位置
                result.add(0, samplingAverage);
            }
            return result;
        }

        //如果原列表的项，大于等于取样精度，小于取样周期，则得到一个取样结果
        if (list.size() >= averageCounts) {
            double samplingSum = 0d;
            for (int i = 0; i < averageCounts; i++) {
                samplingSum = samplingSum + list.get(list.size() - 1 - i);
            }
            double average = samplingSum / averageCounts;
            result.add(average);
            return result;
        }


        //如果原列表的项，小于取样精度，则求取所有项的平均值
        if (list.size() > 0) {
            double samplingSum = 0d;
            for (Double value : list) {
                samplingSum = samplingSum + value;
            }
            double average = samplingSum / list.size();
            result.add(average);
            return result;
        }

        //如果原列表是空的，直接返回
        return result;

    }
}
