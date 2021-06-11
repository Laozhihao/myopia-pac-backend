package com.wupol.myopia.business.common.utils.constant;

/**
 * 缓存相关常量
 * - 为了方便维护和便于Redis可视化工具中排查问题，采用冒号来分割风格
 * - 格式 = 类别:描述(或类别，下划线命名):唯一值描述_唯一值占位符
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public interface QrCodeCacheKey {

    String PARENT_STUDENT_PREFIX = "PA@";
    /**
     * 获取学生二维码
     */
    String PARENT_STUDENT_QR_CODE = "QRCODE:TOKEN_%s";
}
