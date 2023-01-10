package com.wupol.myopia.base.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 行数计算
 *
 * @author Simple4H
 */
@UtilityClass
public class RowSpanUtils {

    public static Integer setRowSpan(AtomicBoolean isFirst, Integer size) {
        if (isFirst.get()) {
            isFirst.set(false);
            return size;
        } else {
            return 0;
        }
    }
}
