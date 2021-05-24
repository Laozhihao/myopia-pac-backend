package com.wupol.myopia.business.api.parent.constant;

/**
 * 缓存相关常量
 * - 为了方便维护和便于Redis可视化工具中排查问题，采用冒号来分割风格
 * - 格式 = 类别:描述(或类别，下划线命名):唯一值描述_唯一值占位符
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public interface SmsCacheKey {
    /**
     * 短信验证码token
     */
    String SMS_CODE_TOKEN = "sms:code:token:phone_%s";
    /**
     * 短信校验失败数量
     */
    String SMS_TOKEN_FAIL_COUNT = "sms:code:fail_count:phone_%s";
}
