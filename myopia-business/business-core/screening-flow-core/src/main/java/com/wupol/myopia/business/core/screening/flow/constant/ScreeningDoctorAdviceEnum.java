package com.wupol.myopia.business.core.screening.flow.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 筛查报告建议
 *
 * @author Simple4H
 */
public enum ScreeningDoctorAdviceEnum {

    SUGGEST_CONTENT_0(null, StringUtils.EMPTY, -1),
    SUGGEST_CONTENT_1(Boolean.TRUE, "戴镜视力下降，非非弱视者建议及时到医疗机构复查，确定是否需要更换眼镜", 2),
    SUGGEST_CONTENT_2(Boolean.FALSE, "戴镜视力正常。建议：3个月或半年1次检查裸眼视力和戴镜视力", 0),
    SUGGEST_CONTENT_3(Boolean.TRUE, "裸眼远视力下降，视功能可能异常。建议:到医疗机构接受检查，明确诊断并及时采取措施", 2),
    SUGGEST_CONTENT_4(Boolean.TRUE, "裸眼远视力下降，屈光不正筛查阳性。建议：到医疗结构接受检查，明确诊断并及时采取措施", 2),
    SUGGEST_CONTENT_5(Boolean.FALSE, "裸眼远视力≥4.9，目前尚无近视高危因素。建议：①6~12个月复查 ②6岁儿童SE（等效球镜）≥2.00D到医疗机构接受检查", 0),
    SUGGEST_CONTENT_6(Boolean.FALSE, "裸眼远视力≥4.9，可能存在近视高危因素。建议：①严格注意用眼卫生 ②到医疗机构接受检查了解是否可能发展为近视", 1);

    /**
     * 是否建议就诊
     */
    @Getter
    private final Boolean isRecommendDoctor;

    /**
     * 建议内容
     */
    @Getter
    private final String suggestContent;

    /**
     * 优先级
     */
    @Getter
    private final Integer priority;

    ScreeningDoctorAdviceEnum(Boolean isRecommendDoctor, String suggestContent, Integer priority) {
        this.isRecommendDoctor = isRecommendDoctor;
        this.suggestContent = suggestContent;
        this.priority = priority;
    }
}
