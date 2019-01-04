package com.huobi.constant;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: squirrel
 * @Date: 18-9-6 下午3:31
 */
public interface HuobiConsts {
    String API_HOST = "api.hbdm.com";
    String API_URL = "https://" + API_HOST;

    String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";

    String ENDPOINT_SECURITY_TYPE_APIKEY = "AccessKeyId";
    String ENDPOINT_SECURITY_TYPE_APIKEY_HEADER = ENDPOINT_SECURITY_TYPE_APIKEY + ":#";

    String ENDPOINT_SECURITY_TYPE_SIGNED = "Signature";
    String ENDPOINT_SECURITY_TYPE_SIGNED_HEADER = ENDPOINT_SECURITY_TYPE_SIGNED + ":#";

    String SIGNATURE_METHOD = "HmacSHA256";
    String SIGNATURE_VERSION = "2";

    DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
    ZoneId ZONE_GMT = ZoneId.of("Z");

    //所有合约交易对
    List<String> ALL_CONTRACT_SYMBOLS = Arrays.asList("BTC_CW", "BTC_NW","BTC_CQ","ETH_CW", "ETH_NW",
            "ETH_CQ","EOS_CW", "EOS_NW","EOS_CQ");

}
