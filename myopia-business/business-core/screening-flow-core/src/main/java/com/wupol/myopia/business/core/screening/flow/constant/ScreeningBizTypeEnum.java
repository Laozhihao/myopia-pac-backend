package com.wupol.myopia.business.core.screening.flow.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 根据业务判断的筛查类型
 *
 * @author hang.yuan 2022/9/16 11:17
 */
public enum ScreeningBizTypeEnum {

    SCHOOL(0,"协助筛查",ScreeningOrgTypeEnum.ORG.getType()),
    ORG(1,"自主筛查",ScreeningOrgTypeEnum.SCHOOL.getType());
    /**
     * 类型
     **/
    @Getter
    private final Integer type;
    /**
     * 描述
     **/
    @Getter
    private final String name;

    @Getter
    private final Integer orgType;

    ScreeningBizTypeEnum(Integer type, String name,Integer orgType) {
        this.type = type;
        this.name = name;
        this.orgType = orgType;
    }

    public static ScreeningBizTypeEnum getInstanceByOrgType(Integer orgType){
        return Arrays.stream(values())
                .filter(item-> Objects.equals(item.getOrgType(),orgType))
                .findFirst().orElse(null);
    }
}
