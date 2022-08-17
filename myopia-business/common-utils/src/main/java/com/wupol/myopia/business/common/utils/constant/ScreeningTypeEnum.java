package com.wupol.myopia.business.common.utils.constant;

import com.wupol.myopia.base.exception.BusinessException;
import lombok.Getter;

import java.util.Arrays;

/**
 * 筛查类型枚举类
 *
 * @Author HaoHao
 * @Date 2022/4/24
 **/
@Getter
public enum ScreeningTypeEnum {

    VISION(0, "视力筛查"),
    COMMON_DISEASE(1, "常见病筛查");

    /**
     * 代码
     */
    private final Integer type;

    /**
     * 描述
     */
    private final String desc;

    ScreeningTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 根据类型获取
     *
     * @param type 筛查类型
     * @return com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum
     **/
    public static ScreeningTypeEnum getByType(Integer type) {
        return Arrays.stream(ScreeningTypeEnum.values())
                .filter(item -> item.type.equals(type))
                .findFirst()
                .orElseThrow(()-> new BusinessException("无效筛查类型"));
    }

    /**
     * 是否为视力筛查类型
     *
     * @param screeningType 筛查类型
     * @return boolean
     **/
    public static boolean isVisionScreeningType(Integer screeningType) {
        return ScreeningTypeEnum.VISION.getType().equals(screeningType);
    }

    /**
     * 是否为常见病筛查类型
     *
     * @param screeningType 筛查类型
     * @return boolean
     **/
    public static boolean isCommonDiseaseScreeningType(Integer screeningType) {
        return ScreeningTypeEnum.COMMON_DISEASE.getType().equals(screeningType);
    }

}
