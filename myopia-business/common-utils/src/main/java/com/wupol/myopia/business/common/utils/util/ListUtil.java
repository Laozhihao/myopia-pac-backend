package com.wupol.myopia.business.common.utils.util;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * list工具
 *
 * @author Simple4H
 */
@UtilityClass
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
     * 多个list取交集
     * @param qesIdList list集合
     */
    public static List<Integer> getIntersection(List<List<Integer>> qesIdList){
        if (CollUtil.isEmpty(qesIdList)){
            return Lists.newArrayList();
        }
        List<Integer> intersectionList = qesIdList.get(0);
        if (qesIdList.size() == 1){
            return intersectionList;
        }
        for (List<Integer> qesIds : qesIdList) {
            intersectionList.retainAll(qesIds);
        }
        return intersectionList;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new HashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
