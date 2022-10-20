package com.wupol.myopia.business.aggregation.student.constant;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.LowVisionLevelEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 视力情况 (暂时用于学校学生)
 *
 * @author hang.yuan 2022/10/12 20:35
 */
public enum VisionSituationEnum {

    NORMAL(0,"正常", LowVisionLevelEnum.ZERO.getCode(),null),
    LOW_VISION_KINDERGARTEN(1,"视力低常", LowVisionLevelEnum.LOW_VISION.getCode(),Boolean.TRUE),
    LOW_VISION_PRIMARY_ABOVE(2,"视力低下", LowVisionLevelEnum.LOW_VISION.getCode(),Boolean.FALSE);

    @Getter
    private Integer code;
    @Getter
    private String desc;
    @Getter
    private Integer type;
    @Getter
    private Boolean isKindergarten;


    VisionSituationEnum(Integer code, String desc, Integer type, Boolean isKindergarten) {
        this.code = code;
        this.desc = desc;
        this.type = type;
        this.isKindergarten = isKindergarten;
    }

    public static VisionSituationEnum getByCode(Integer code){
        return Arrays.stream(values())
                .filter(item -> Objects.equals(item.getCode(),code))
                .findFirst().orElseThrow(()->new BusinessException("暂无此类型"));
    }

    /**
     * 获取幼儿园/小学及以上的对应值
     * @param condition 是否幼儿园
     */
    public static List<VisionSituationEnum> listByCondition(Boolean condition){
        Stream<VisionSituationEnum> stream = Arrays.stream(values());
        if (Objects.nonNull(condition)){
            stream = stream.filter(item -> !Objects.equals(item.getIsKindergarten(),!condition));
        }
        return stream.collect(Collectors.toList());
    }
}
