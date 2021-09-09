package com.wupol.myopia.business.common.utils.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ID 生成器工具
 *
 * @Author HaoHao
 * @Date 2021/9/9
 **/
public class IdUtil {

    /** 生成器 */
    private static AtomicLong generator;

    private IdUtil() {}

    /**
     * 获取生成器实例
     *
     * @return java.util.concurrent.atomic.AtomicLong
     **/
    private static synchronized AtomicLong getGeneratorInstance() {
        if (generator == null) {
            String offset = StringUtils.substring(Long.toString(System.currentTimeMillis()), 1);
            generator = new AtomicLong(Long.valueOf(offset));
        }
        return generator;
    }

    /**
     * 批量生成ID
     *
     * @param amount 数量
     * @return java.util.List<java.lang.Long>
     **/
    public static List<Long> getIdBatch(Long amount) {
        List<Long> ids = new ArrayList<>();
        AtomicLong idGenerator = getGeneratorInstance();
        for (int i = 0; i < amount; i++) {
            ids.add(idGenerator.incrementAndGet());
        }
        return ids;
    }

    /**
     * 获取下一个ID
     *
     * @return java.lang.Long
     **/
    public static Long nextId() {
        return getGeneratorInstance().incrementAndGet();
    }
}
