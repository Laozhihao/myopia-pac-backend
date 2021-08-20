package com.wupol.myopia.business.common.utils.constant;

import lombok.experimental.UtilityClass;

/**
 * 业务信息常量
 *
 * @author Simple4H
 */
@UtilityClass
public class BizMsgConstant {

    public final String SAVE_DIRECTORY_EMPTY = "文件保存目录路径为空";

    public final String NOTICE_ID_IS_EMPTY = "筛查通知ID为空";

    public final String PLAN_ID_IS_EMPTY = "筛查计划ID为空";

    public final String VALIDATION_START_TIME_ERROR = "筛查开始时间不能早于今天";

    public final String CAN_NOT_FIND_NOTICE = "找不到该notice";

    public final String NOT_ADMIN_NO_ACCESS = "非平台管理员，没有访问权限";

    public final String NO_ACCESS = "没有访问权限";
}