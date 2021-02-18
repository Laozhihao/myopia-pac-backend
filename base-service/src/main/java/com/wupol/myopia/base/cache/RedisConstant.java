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
     * 用户权限缓存key，auth:user:permission:{userId}，如：auth:user:permission:24
     */
    String USER_PERMISSION_KEY = "auth:user:permission:%d";
}
