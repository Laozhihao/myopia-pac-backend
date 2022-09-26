package com.wupol.myopia.base.util;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 总部账号配置工具类
 *
 * @author Simple4H
 */
@UtilityClass
public class OverviewConfigUtil {

    private static Map<String, Integer> getOverviewConfigMap() {
        List<String> list = Lists.newArrayList("org", "hospital", "school");
        HashMap<String, Integer> result = Maps.newHashMap();
        Integer type = 0;
        int size = list.size();
        for (int i = 1; i <= (1 << size) - 1; i++) {
            StringBuilder stringBuffer = new StringBuilder();
            for (int j = 0; j < size; j++) {
                if ((i >> j & 1) == 1) {
                    stringBuffer.append(list.get(j)).append(StrUtil.COMMA);
                }
            }
            result.put(stringBuffer.toString(), type);
            type++;
        }
        return result;
    }

    private static Map<Integer, String> overviewConfigKeyMap() {
        return getOverviewConfigMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * List转换成configType
     *
     * @param configTypeLists 配置类型List
     *
     * @return 类型
     */
    public static Integer list2configType(List<String> configTypeLists) {
        return getOverviewConfigMap().get(String.join(StrUtil.COMMA, configTypeLists) + StrUtil.COMMA);
    }

    /**
     * configType转换成List
     *
     * @param configType 配置类型
     *
     * @return List
     */
    public static List<String> configTypeList(Integer configType) {
        String x = StringUtils.removeEnd(overviewConfigKeyMap().get(configType), StrUtil.COMMA);
        return Arrays.stream(x.split(",")).collect(Collectors.toList());
    }
}
