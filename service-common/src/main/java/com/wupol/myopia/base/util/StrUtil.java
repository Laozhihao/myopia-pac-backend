package com.wupol.myopia.base.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 *
 * @author Simple4H
 */
public class StrUtil {

    /**
     * 拼接字符
     *
     * @param str     字符
     * @param strings string
     * @return 字符
     */
    public static String spliceChar(String str, String... strings) {
        if (Objects.isNull(strings) || strings.length == 0) {
            return StringUtils.EMPTY;
        }
        return Arrays.stream(strings).filter(Objects::nonNull).collect(Collectors.joining(str));
    }
}
