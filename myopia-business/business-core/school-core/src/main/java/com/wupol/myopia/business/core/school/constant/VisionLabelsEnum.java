package com.wupol.myopia.business.core.school.constant;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 视力标签枚举类
 *
 * @author Simple4H
 */
@Getter
public enum VisionLabelsEnum {

    NORMAL_VISION(1, "视力正常"),
    LOW_VISION(2, "视力低下"),
    MILD_VISION_LOSS(3, "轻度视力低下"),
    MODERATELY_VISION_LOSS(4, "中度视力低下"),
    SEVERE_VISION_LOSS(5, "重度视力低下"),
    MILD_MYOPIA(6, "轻度近视"),
    MODERATE_MYOPIA(7, "中度近视"),
    HIGH_MYOPIA(8, "高度近视"),
    MILD_HYPEROPIA(9, "轻度远视"),
    MODERATE_HYPEROPIA(10, "中度远视"),
    HIGH_HYPEROPIA(11, "高度远视"),
    MILD_ASTIGMATISM(12, "轻度散光"),
    MODERATE_ASTIGMATISM(13, "中度散光"),
    HIGH_ASTIGMATISM(14, "高度散光"),
    BAD_CROWD(15, "不良人群"),
    LEVEL_ONE_WARNING(16, "一级预警"),
    LEVEL_TWO_WARNING(17, "二级预警"),
    LEVEL_THREE_WARNING(18, "三级预警");


    private final Integer code;

    private final String name;

    VisionLabelsEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取视力标签列表
     *
     * @return 视力标签列表
     */
    public static List<VisionLabels> getVisionLabels() {
        List<VisionLabels> visionLabels = new ArrayList<>();
        for (VisionLabelsEnum value : values()) {
            VisionLabels labels = new VisionLabels();
            labels.setCode(value.getCode());
            labels.setName(value.getName());
            labels.setValue(value.toString());
            visionLabels.add(labels);
        }
        return visionLabels;
    }
}
