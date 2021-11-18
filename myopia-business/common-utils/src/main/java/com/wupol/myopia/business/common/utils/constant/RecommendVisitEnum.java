package com.wupol.myopia.business.common.utils.constant;

import lombok.Getter;

/**
 * 就诊判断
 *
 * @author Simple4H
 */
@Getter
public enum RecommendVisitEnum {

    EMPTY(0, false, ""),
    KINDERGARTEN_RESULT_1(1, true, "戴镜视力下降，建议：及时到医疗机构复查，确定是否需要更换眼镜。"),
    KINDERGARTEN_RESULT_2(2, false, "戴镜视力正常。建议：3个月或半年1次检查裸眼视力和戴镜视力。"),
    KINDERGARTEN_RESULT_3(3, true, "裸眼视力下降，视功能可能异常。建议：到医疗机构接受散瞳光检查，明确诊断并采取措施。"),
    KINDERGARTEN_RESULT_4(4, true, "裸眼视力下降，合并较为明显的屈光不正或眼病。建议：到医疗机构明确诊断及时矫治疗。"),
    MIDDLE_RESULT_1(5, true, "戴镜视力下降。建议：非弱势者及时到医疗机构复查，确定是否需要更换眼镜。"),
    MIDDLE_RESULT_2(6, false, "戴镜视力正常。建议：请3个月或者半年1次检查裸眼视力和戴镜视力。"),
    MIDDLE_RESULT_3(7, true, "裸眼远视力下降，视功能可能异常。建议：请到医疗机构接受检查，明确诊断并及时采取措施。"),
    MIDDLE_RESULT_4(8, true, "裸眼视力下降，屈光不正筛查阳性。建议：到医疗机构明确诊断及时矫治疗。"),
    MIDDLE_RESULT_5(9, false, "裸眼远视力≥4.9，目前尚无近视高危因素。建议：6～12个月复查。"),
    MIDDLE_RESULT_6(10, true, "裸眼远视力≥4.9，目前尚无近视高危因素。但远视度数较高，可能存在远视高位因素。建议：请到医疗机构接受检查。"),
    MIDDLE_RESULT_7(11, true, "裸眼远视力≥4.9，可能存在近视高危因素。建议：1）严格注意用 眼卫生；2）到医疗机构接受检查了解是否可能发展为近视。"),
    KINDERGARTEN_RESULT_5(12, true, "远视初步不止，有发生近视的可能性。建议：需进一步检查，改变不良用眼行为。");

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 是否建议就诊
     */
    private final Boolean isRecommendVisit;

    /**
     * 建议
     */
    private final String advice;

    RecommendVisitEnum(Integer type, Boolean isRecommendVisit, String advice) {
        this.type = type;
        this.isRecommendVisit = isRecommendVisit;
        this.advice = advice;
    }
}
