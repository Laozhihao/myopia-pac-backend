package com.wupol.myopia.business.common.utils.util;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * map工具类
 *
 * @author hang.yuan 2022/4/20 12:23
 */
public class MyopiaMapUtil extends cn.hutool.core.map.MapUtil {

    /**
     *  分割Map大小
     * @author hang.yuan
     * @date 2022/4/2
     */
    public static <K, V> List<Map<K, V>> splitMap(Map<K, V> map, int pageSize) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        pageSize = pageSize == 0 ? 50000 : pageSize;
        List<Map<K, V>> newList = new ArrayList<>();
        int j = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (j % pageSize == 0) {
                newList.add(Maps.newHashMap());
            }
            newList.get(newList.size() - 1).put(entry.getKey(), entry.getValue());
            j++;
        }
        return newList;
    }
}
