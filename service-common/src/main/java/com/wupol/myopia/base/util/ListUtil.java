package com.wupol.myopia.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * list工具
 *
 * @author Simple4H
 */
public class ListUtil {

    /**
     * list中重复的元素
     *
     * @param list list
     * @param <T>  对象
     * @return 重复的元素
     */
    public static <T> List<T> getDuplicateElements(List<T> list) {
        return list.stream()
                .collect(Collectors.toMap(e -> e, e -> 1, Integer::sum))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * List转换成String
     *
     * @param obj List
     * @return String
     */
    public static String objectList2Str(Object obj) {
        List<String> result = new ArrayList<>();
        if (obj instanceof ArrayList<?>) {
            for (Object o : (List<?>) obj) {
                result.add((String) o);
            }
        }
        return String.join(",", result);
    }
}
