package com.huobi.utils.jsonSerilizableUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;

/**
 * @Description 序列化和反序列化对象到文件
 * @Author squirrel
 * @Date 18-10-9 上午10:13
 */
public class JsonSerializable {

    /* 将对象序列化为字符串存入json文件中 */
    public static String serializeToFile(Object obj, String OutfilePathName)
            throws IOException {
        String string = JSONUtil.writeValue(obj);
        ReadAndWriteJson.writeFile(OutfilePathName, string);
        return string;
    }

    // 判断文件内容是否为空
    public static boolean isFileEmpty(String InputfilePathName) throws IOException {
        String string = ReadAndWriteJson.readFile(InputfilePathName);
        return string.trim().length() == 0;
    }

    /* 将json文件中的内容读取出来，反序列化为对象 */
    public static <T> T deserializeListFromFile(String InputfilePathName, TypeReference<T> ref) throws IOException {
        String string = ReadAndWriteJson.readFile(InputfilePathName);
        return JSONUtil.readValue(string, ref);
    }

}


