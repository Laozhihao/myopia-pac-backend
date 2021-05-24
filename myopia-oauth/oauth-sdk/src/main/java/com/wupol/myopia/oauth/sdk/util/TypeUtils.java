package com.wupol.myopia.oauth.sdk.util;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.myopia.base.exception.BusinessException;
import lombok.experimental.UtilityClass;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author jacob
 * @version 1.0.0
 * Detail: json通过类型转换成特定的类
 * @date 2020/11/6 17:37
 */
@UtilityClass
public class TypeUtils {

    /**
     * 根据类型，将json转换成对应类型的对象
     * @param type
     * @param data
     * @return
     * @throws JsonProcessingException
     */
    public Object getObject(Type type, String data) throws JsonProcessingException {
        if (type == null || data == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        if (type instanceof ParameterizedTypeImpl) {
            ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) type;
            Class<?> rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Type genericType = Arrays.stream(actualTypeArguments).findFirst().orElseThrow(()-> new BusinessException("参数为空,type = " + JSON.toJSONString(type)));
            Class<?> genericClass;
            try {
                genericClass = Class.forName(genericType.getTypeName());
            } catch (ClassNotFoundException e) {
                // 修改为runtimeException
                throw new IllegalStateException("无法找到类 " + genericType.getTypeName(), e);
            }
            if (Collection.class.isAssignableFrom(rawType)) {
                JavaType javaType = getCollectionType(rawType, genericClass);
                return mapper.readValue(data, javaType);
            } else {
                return mapper.readValue(data, rawType);
            }
        } else {
            return mapper.readValue(data, new ObjectMapper().getTypeFactory().constructType(type));
        }
    }

    /**
     * 获取集合类型
     *
     * @param collectionClass
     * @param elementClasses
     * @return
     */
    private static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return new ObjectMapper().getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}
