package com.wupol.myopia.business.common.utils.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.myopia.business.common.utils.exception.SerializeErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Objects;

/**
 *
 * @author Alix
 * @date 2019-01-17
 */
public class SerializationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializationUtil.class);
    /**
     * 数据序列化
     * @param object
     * @return
     * @throws SerializeErrorException
     */
    public static String serialize(Object object) throws SerializeErrorException {
        try {
            return Objects.isNull(object) ? "" : new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new SerializeErrorException(String.format("序列化失败，object: %s", object), e);
        }
    }

    /**
     * 数据序列化
     * 出错时，返回空对象
     * @param object
     * @return
     */
    public static String serializeWithoutException(Object object) {
        try {
            return Objects.isNull(object) ? "" : new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            LOGGER.error(String.format("序列化失败，object: %s", object), e);
            return object instanceof Collection ? "[]" : "{}";
        }
    }

    /**
     * 数据反序列化
     * 异常统一抛出SerializeErrorException
     * @param str
     * @param valueType
     * @param <T>
     * @return
     * @throws SerializeErrorException
     */
    public static <T> T deserialize(String str, Class<T> valueType) throws SerializeErrorException {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new SerializeErrorException(String.format("反序列化失败，object: %s", str), e);
        }
    }

    /**
     * 数据反序列化
     * 异常统一抛出SerializeErrorException
     * @param str
     * @param <T>
     * @return
     * @throws SerializeErrorException
     */
    public static <T> T deserializeToList(String str, TypeReference<T> tr) throws SerializeErrorException {
        try {
            return new ObjectMapper().readValue(str, tr);
        } catch (Exception e) {
            throw new SerializeErrorException(String.format("反序列化失败，object: %s", str), e);
        }
    }

    /**
     * 数据反序列化
     * 异常统一抛出SerializeErrorException
     * @param str
     * @param valueType
     * @param <T>
     * @return
     * @throws SerializeErrorException
     */
    public static <T> T deserializeWithoutException(String str, Class<T> valueType) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            LOGGER.error(String.format("反序列化失败，str: %s", str), e);
            return null;
        }
    }
}
