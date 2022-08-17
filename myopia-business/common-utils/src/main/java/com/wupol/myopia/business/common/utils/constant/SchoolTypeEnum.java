package com.wupol.myopia.business.common.utils.constant;

import com.wupol.myopia.base.exception.BusinessException;
import lombok.Getter;

import java.util.Arrays;

/**
 * 学校类型
 *
 * @Author HaoHao
 * @Date 2022/6/16
 **/
@Getter
public enum SchoolTypeEnum {

    PRIMARY_AND_SECONDARY(1, "中小学"),
    UNIVERSITY(2, "大学"),
    KINDERGARTEN(3, "幼儿园");

    private Integer type;
    private String desc;

    SchoolTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static SchoolTypeEnum getByType(Integer type) {
        return Arrays.stream(SchoolTypeEnum.values())
                .filter(item -> item.type.equals(type))
                .findFirst()
                .orElseThrow(() -> new BusinessException("无效学校类型"));
    }
}
