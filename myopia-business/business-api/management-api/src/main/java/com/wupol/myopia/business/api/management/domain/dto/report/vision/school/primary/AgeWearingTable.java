package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import lombok.Getter;
import lombok.Setter;

/**
 * 戴镜类型表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AgeWearingTable {

    /**
     * 项目
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 不佩戴眼镜
     */
    private Long notWearing;

    /**
     * 不佩戴眼镜-占比
     */
    private String notWearingProportion;

    /**
     * 佩戴框架眼镜
     */
    private Long glasses;

    /**
     * 佩戴框架眼镜-占比
     */
    private String glassesProportion;

    /**
     * 佩戴隐形眼镜
     */
    private Long wearingContact;

    /**
     * 佩戴隐形眼镜-占比
     */
    private String wearingContactProportion;

    /**
     * 夜戴
     */
    private Long nightWearing;

    /**
     * 夜戴-占比
     */
    private String nightWearingProportion;

    /**
     * 足矫
     */
    private Long enough;

    /**
     * 足矫-占比
     */
    private String enoughProportion;

    /**
     * 未矫
     */
    private Long uncorrected;

    /**
     * 未矫-占比
     */
    private String uncorrectedProportion;

    /**
     * 欠矫
     */
    private Long under;

    /**
     * 欠矫-占比
     */
    private String underProportion;
}
