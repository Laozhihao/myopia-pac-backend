package com.wupol.myopia.business.aggregation.student.constant;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.*;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 屈光情况 （暂时用于学校学生）
 *
 * @author hang.yuan 2022/10/12 20:03
 */
public enum RefractionSituationEnum {

    NORMAL(0,"正常", LowVisionLevelEnum.ZERO.getCode(),null),

    MYOPIA_LEVEL_EARLY(1, "近视前期", MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.getCode(),Boolean.FALSE),
    MYOPIA(2, "近视",-1,Boolean.FALSE),
    MYOPIA_LEVEL_LIGHT(3, "低度近视",MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.getCode(),Boolean.FALSE),
    MYOPIA_LEVEL_HIGH(4, "高度近视",MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.getCode(),Boolean.FALSE),

    HYPEROPIA(5, "远视", -1,Boolean.FALSE),
    HYPEROPIA_LEVEL_LIGHT(6, "低度远视",HyperopiaLevelEnum.HYPEROPIA_LEVEL_LIGHT.getCode(),Boolean.FALSE),
    HYPEROPIA_LEVEL_MIDDLE(7, "中度远视",HyperopiaLevelEnum.HYPEROPIA_LEVEL_MIDDLE.getCode(),Boolean.FALSE),
    HYPEROPIA_LEVEL_HIGH(8, "重度远视",HyperopiaLevelEnum.HYPEROPIA_LEVEL_HIGH.getCode(),Boolean.FALSE),

    ASTIGMATISM(9,"散光",-1,Boolean.FALSE),
    ASTIGMATISM_LEVEL_LIGHT(10, "低度散光",AstigmatismLevelEnum.ASTIGMATISM_LEVEL_LIGHT.getCode(),Boolean.FALSE),
    ASTIGMATISM_LEVEL_MIDDLE(11, "中度散光",AstigmatismLevelEnum.ASTIGMATISM_LEVEL_MIDDLE.getCode(),Boolean.FALSE),
    ASTIGMATISM_LEVEL_HIGH(12, "高度散光", AstigmatismLevelEnum.ASTIGMATISM_LEVEL_HIGH.getCode(),Boolean.FALSE),

    INSUFFICIENT(13,"远视储备不足", WarningLevel.ZERO_SP.getCode(),Boolean.TRUE),
    REFRACTIVE_ERROR(14,"屈光不足",1,Boolean.TRUE),
    ANISOMETROPIA(15,"屈光参差",1,Boolean.TRUE);


    @Getter
    private Integer code;
    @Getter
    private String desc;
    @Getter
    private Integer type;
    @Getter
    private Boolean isKindergarten;

    RefractionSituationEnum(Integer code, String desc, Integer type, Boolean isKindergarten) {
        this.code = code;
        this.desc = desc;
        this.type = type;
        this.isKindergarten = isKindergarten;
    }

    public static RefractionSituationEnum getByCode(Integer code){
        return Arrays.stream(values())
                .filter(item -> Objects.equals(item.getCode(),code))
                .findFirst().orElseThrow(()->new BusinessException("暂无此类型"));
    }

    /**
     * 获取幼儿园/小学及以上的对应值
     * @param condition 是否幼儿园
     */
    public static List<RefractionSituationEnum> listByCondition(Boolean condition){
        Stream<RefractionSituationEnum> stream = Arrays.stream(values());
        if (Objects.nonNull(condition)){
            stream = stream.filter(item -> !Objects.equals(item.getIsKindergarten(),!condition));
        }
        return stream.collect(Collectors.toList());
    }
}
