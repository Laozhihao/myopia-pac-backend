package com.wupol.myopia.base.cache;

/**
 * Redis缓存key
 *
 * @Author HaoHao
 * @Date 2020/12/26
 **/
public interface RedisConstant {
    /**
     * 所有系统权限
     */
    String ALL_PERMISSION_KEY = "auth:permission:all";

    /**
     * 单个系统权限
     */
    String SINGLE_SYSTEM_PERMISSION_KEY_PREFIX = "auth:single:permission:";
}
