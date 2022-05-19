package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 戴镜类型表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GenderWearingTable {

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
    private CountAndProportion notWearing;

    /**
     * 不佩戴眼镜-占比
     */
    private CountAndProportion notWearingProportion;

    /**
     * 佩戴框架眼镜
     */
    private CountAndProportion wearingFrame;

    /**
     * 佩戴框架眼镜-占比
     */
    private CountAndProportion wearingFrameProportion;

    /**
     * 佩戴隐形眼镜
     */
    private CountAndProportion wearingContact;

    /**
     * 佩戴隐形眼镜-占比
     */
    private CountAndProportion wearingContactProportion;

    /**
     * 夜戴
     */
    private CountAndProportion nightWearing;

    /**
     * 夜戴-占比
     */
    private CountAndProportion nightWearingProportion;

    /**
     * 足矫
     */
    private CountAndProportion enough;

    /**
     * 足矫-占比
     */
    private CountAndProportion enoughProportion;

    /**
     * 未矫
     */
    private CountAndProportion notYet;

    /**
     * 未矫-占比
     */
    private CountAndProportion notYetProportion;

    /**
     * 欠矫
     */
    private CountAndProportion owe;

    /**
     * 欠矫-占比
     */
    private CountAndProportion oweProportion;
}
