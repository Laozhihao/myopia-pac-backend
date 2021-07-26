package com.wupol.myopia.base.util;


import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.exception.BusinessException;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BeanCopyUtil {
    public static <T> List<T> deepCopyListProperties(List<?> list, Class<T> clazz) {
        String oldOb = JSON.toJSONString(list);
        return JSON.parseArray(oldOb, clazz);
    }

    public static <T> Set<T> deepCopySetProperties(Set<?> set, Class<T> clazz) {
        List<?> list = new ArrayList<>(set);
        List<T> copyList = deepCopyListProperties(list, clazz);
        return new HashSet<>(copyList);
    }

    public static <T> T copyBeanPropertise(Object src, Class<T> clazz) {
        if (src == null) {
            return null;
        } else {
            try {
                T target = clazz.newInstance();
                BeanUtils.copyProperties(src, target);
                return target;
            } catch (IllegalAccessException | InstantiationException var4) {
                throw new BusinessException("copyBeanPropertise出错", var4);
            }
        }
    }

    private BeanCopyUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

