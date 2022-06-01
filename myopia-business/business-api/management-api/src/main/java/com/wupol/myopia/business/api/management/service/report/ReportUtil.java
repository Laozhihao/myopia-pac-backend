package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import lombok.experimental.UtilityClass;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 报告工具
 *
 * @author hang.yuan 2022/6/1 09:45
 */
@UtilityClass
public class ReportUtil {


    /**
     * 获取map中Value最大值及对应的Key
     */
    public  <T,K>TwoTuple<K,String> getMaxMap(Map<K, T> map, Function<T,Integer> function,Function<T,String> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0)- Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
    }

    /**
     * 获取map中Value最小值及对应的Key
     */
    public  <T,K> TwoTuple<K,String> getMinMap(Map<K, T> map, Function<T,Integer> function, Function<T,String> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries, Comparator.comparingInt(o -> Optional.ofNullable(o.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
    }

    public static Integer getLessAge(Integer age){
        if (age < 3){
            return 3;
        }else if (age < 4){
            return 4;
        }else if (age < 5){
            return 5;
        }else if (age < 6){
            return 6;
        }else if (age < 7){
            return 7;
        }else if (age < 8){
            return 8;
        }else if (age < 9){
            return 9;
        }else if (age < 10){
            return 10;
        }else if (age < 11){
            return 11;
        }else if (age < 12){
            return 12;
        }else if (age < 13){
            return 13;
        }else if (age < 14){
            return 14;
        }else if (age < 15){
            return 15;
        }else if (age < 16){
            return 16;
        }else if (age < 17){
            return 17;
        }else if (age < 18) {
            return 18;
        }else {
            return 19;
        }
    }
}
