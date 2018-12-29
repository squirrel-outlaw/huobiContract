package com.huobi.utils;

import com.huobi.utils.jsonSerilizableUtils.JSONUtil;

import java.io.IOException;

/**
 * @Description
 * @Author squirrel
 * @Date 18-9-19 下午4:06
 */
public class PrintUtil {

    public static void print(Object obj) {
        try {
            System.out.println(JSONUtil.writeValue(obj));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
