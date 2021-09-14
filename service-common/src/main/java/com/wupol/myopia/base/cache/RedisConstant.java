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
     * 用户权限资源，auth:user:permission:{userId}，如：auth:user:permission:24
     */
    String USER_PERMISSION_KEY = "auth:user:permission:%d";

    /**
     * 用户授权token，auth:user:authorization:{userId}，如：auth:user:authorization:24
     */
    String USER_AUTHORIZATION_KEY = "auth:user:authorization:%d";

    /**
     * 学生二维码过期时间
     */
    Integer TOKEN_EXPIRE_TIME = 3600;

    /**
     * 正在导出Key
     */
    String FILE_EXPORT_ING = "file:export:ing";

    /**
     * 文件导出Key
     */
    String FILE_EXPORT_LIST = "file:export:list";

    /**
     * 筛查通知
     */
    String FILE_EXPORT_NOTICE_DATA = "file:export:notice:%s-%s-%s-%s-%s-%s";

    /**
     * 筛查计划
     */
    String FILE_EXPORT_PLAN_DATA = "file:export:plan:%s-%s-%s-%s";
}
