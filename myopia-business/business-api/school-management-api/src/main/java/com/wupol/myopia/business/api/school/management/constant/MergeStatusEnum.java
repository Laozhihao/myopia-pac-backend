package com.wupol.myopia.business.api.school.management.constant;

import lombok.Getter;

/**
 * 合并状态
 *
 * @author hang.yuan 2022/9/20 15:33
 */
public enum MergeStatusEnum {
    NOT_RELEASE(0,"未发布"),
    NOT_START(1,"未开始"),
    PROCESSING(2,"进行中"),
    END(3,"已结束");

    @Getter
    private final Integer code;
    @Getter
    private final String desc;

    MergeStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
