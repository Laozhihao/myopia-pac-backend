package com.wupol.myopia.business.common.utils.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.TimeZone;

/**
 * @Description
 * @Date 2021/1/25 9:51
 * @Author by Jacob
 */
@UtilityClass
public class JsonUtil {

    private TimeZone CHINA_TIME_ZONE = TimeZone.getTimeZone("GMT+8");

    public <T> T jsonToObject(String json, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setTimeZone(CHINA_TIME_ZONE);
        return mapper.readValue(json, clazz);
    }

    public String objectToJsonString(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new ManagementUncheckedException(ex, "json转换string错误");
        }
    }

    /**
     * 获取json字符串中指定key的值
     *
     * @param json   json字符串
     * @param k      key
     * @param vClass 值类型
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> T getValue(String json, String k, Class<T> vClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode node = mapper.readTree(json);
        JsonNode data = node.get(k);
        ObjectReader reader = mapper.readerFor(vClass);
        return reader.readValue(data);
    }

    public <T> T parsing(String value, Class<?> parametrized, Class<?>... parameterClasses) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JavaType javaType = mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
        return mapper.readValue(value, javaType);
    }

    public <T> T parsing(TypeReference<?> type, String value) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ObjectReader reader = mapper.readerFor(type);
        return reader.readValue(value);
    }

}
