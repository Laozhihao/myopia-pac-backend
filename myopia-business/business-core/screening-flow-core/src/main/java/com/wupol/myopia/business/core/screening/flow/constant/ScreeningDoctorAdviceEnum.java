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
    SUGGEST_CONTENT_1(Boolean.TRUE, "戴镜视力下降，非弱视者建议及时到医疗机构复查，确定是否需要更换眼镜。\n来源：卫生健康委发布的《儿童青少年近视防控适宜技术指南（更新版）》及解读【2021】", 3),
    SUGGEST_CONTENT_2(Boolean.FALSE, "戴镜视力正常。建议：3个月或半年1次检查裸眼视力和戴镜视力", 1),
    SUGGEST_CONTENT_3(Boolean.TRUE, "裸眼远视力下降，视功能可能异常。建议:到医疗机构接受检查，明确诊断并及时采取措施。\n来源：卫生健康委发布的《儿童青少年近视防控适宜技术指南（更新版）》及解读【2021】", 4),
    SUGGEST_CONTENT_4(Boolean.TRUE, "裸眼远视力下降，屈光不正筛查阳性。建议：到医疗结构接受检查，明确诊断并及时采取措施。\n来源：卫生健康委发布的《儿童青少年近视防控适宜技术指南（更新版）》及解读【2021】", 5),
    SUGGEST_CONTENT_5(Boolean.FALSE, "裸眼远视力≥4.9，目前尚无近视高危因素。建议：①6~12个月复查 ②6岁儿童SE（等效球镜）≥2.00D到医疗机构接受检查", 0),
    SUGGEST_CONTENT_6(Boolean.TRUE, "裸眼远视力≥4.9，可能存在近视高危因素。建议：①严格注意用眼卫生②到医疗机构接受检查了解是否可能发展为近视。\n来源：卫生健康委发布的《儿童青少年近视防控适宜技术指南（更新版）》及解读【2021】", 2),

    SUGGEST_CONTENT_7(Boolean.TRUE, "戴镜视力下降。建议及时到医疗机构复查，确定是否需要更换眼镜。\n来源：卫生健康委发布的《儿童青少年近视防控适宜技术指南（更新版）》及解读【2021】\n", 2),
    SUGGEST_CONTENT_8(Boolean.FALSE, "戴镜视力正常。建议3个月或半年1次检查裸眼视力和戴镜视力", 0),
    SUGGEST_CONTENT_9(Boolean.TRUE, "裸眼视力下降，视功能可能异常。建议到医疗机构接受散瞳光检查，明确诊断并采取措施。\n来源：卫生健康委发布的《儿童青少年近视防控适宜技术指南（更新版）》及解读【2021】", 2),
    SUGGEST_CONTENT_10(Boolean.FALSE, "远视储备不足，有发生近视的可能性，需进一步检查，改变不良用眼行为", 1),
    SUGGEST_CONTENT_11(Boolean.TRUE, "裸眼视力下降，合并较为明显的屈光不正或眼病。建议到医疗机构明确诊断及时矫治疗。\n来源：卫生健康委发布的《儿童青少年近视防控适宜技术指南（更新版）》及解读【2021", 2);

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
