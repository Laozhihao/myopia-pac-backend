package com.wupol.myopia.business.common.utils.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2021/6/30 18:11
 */
public class ObjectUtil {

    /**
     * 是否部分对象为null
     *
     * <pre>
     * ObjectUtil.hasSomeNull("a", null)           = true
     * ObjectUtil.hasSomeNull("a", "a")            = false
     * ObjectUtil.hasSomeNull(null, null)          = false
     * </pre>
     *
     * @param objs
     * @return
     */
    public static boolean hasSomeNull(Object... objs) {
        long nullCount = Arrays.stream(objs).filter(Objects::isNull).count();
        return nullCount > 0 && nullCount < objs.length;
    }

}
