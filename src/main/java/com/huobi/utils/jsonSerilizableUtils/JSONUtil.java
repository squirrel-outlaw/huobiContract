package com.huobi.utils.jsonSerilizableUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

/**
 * @Description
 * @Author squirrel
 * @Date 18-9-19 下午3:53
 */
public class JSONUtil {

    public static String writeValue(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T readValue(String s, TypeReference<T> ref) throws IOException {
        return objectMapper.readValue(s, ref);
    }

    static final ObjectMapper objectMapper = createObjectMapper();

    static ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        //此项功能不打开，对象里含有列表，列表是单个值时，反序列化时会报错
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // disabled features:
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

}
