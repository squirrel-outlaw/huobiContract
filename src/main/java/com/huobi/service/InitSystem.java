package com.huobi.service;

import com.huobi.api.HuobiContractAPI;
import com.huobi.api.impl.HuobiContractAPIImpl;

import com.huobi.utils.security.AESEncryption;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.huobi.utils.DateUtil.getHourPointTimestamp;

/**
 * @Author: squirrel
 * @Date: 18-9-14 下午4:15
 */
public class InitSystem {
    public HuobiContractAPI huobiContractAPI;
    public long tomorrowZeroHourTimestamp; //第二天零点时候的时间戳

    public InitSystem() {
        String[] APIKeysEncrypted = getAPIKeys();
        AESEncryption aesEncryption = new AESEncryption("squirrel");
        String API_KEY = aesEncryption.decrypt(APIKeysEncrypted[0]);
        String API_SECRET = aesEncryption.decrypt(APIKeysEncrypted[1]);
        this.huobiContractAPI = new HuobiContractAPIImpl(API_KEY, API_SECRET);

        //初始化第二天零点时候的时间戳
        this.tomorrowZeroHourTimestamp = getHourPointTimestamp(24);
    }

    private String[] getAPIKeys() {
        InputStream inStream = InitSystem.class.getClassLoader().getResourceAsStream("API.properties");
        Properties prop = new Properties();
        try {
            prop.load(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{prop.getProperty("API_KEY"), prop.getProperty("API_SECRET")};
    }

}


