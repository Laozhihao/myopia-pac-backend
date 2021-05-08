package com.wupol.myopia.business.common.utils.interfaces;

/**
 * 判断是否是有效数据的接口
 * @Description
 * @Date 2021/2/8 21:31
 * @Author by Jacob
 */
public interface ValidResultDataInterface {
    /**
     * 判断是否是有效数据,默认是true
     */
    default boolean judgeValidData() {
        return true;
    }
}
