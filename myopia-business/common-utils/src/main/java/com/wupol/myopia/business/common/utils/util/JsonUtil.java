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

    private TimeZone chinaTimeZone = TimeZone.getTimeZone("GMT+8");

    public <T> T jsonToObject(String json, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setTimeZone(chinaTimeZone);
        try {
            return mapper.readValue(json, clazz);
        }catch (IOException ex) {
            throw new ManagementUncheckedException(ex, "string转换Object错误");
        }
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
     * @return T
     */
    public <T> T getValue(String json, String k, Class<T> vClass) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            JsonNode node = mapper.readTree(json);
            JsonNode data = node.get(k);
            ObjectReader reader = mapper.readerFor(vClass);
            return reader.readValue(data);
        }catch (IOException ex) {
            throw new ManagementUncheckedException(ex, "获取json字符串中指定key的值错误");
        }
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
