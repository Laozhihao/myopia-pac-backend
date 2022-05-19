package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 中小学筛查表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimaryScreeningInfoTable {

    /**
     * 项目
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 人数-视力低下
     */
    private Integer lowVisionCount;

    /**
     * 占比-视力低下/视力低常
     */
    private Integer lowVisionProportion;

    /**
     * 平均视力
     */
    private String avgVision;

    /**
     * 人数-近视
     */
    private Integer myopiaCount;

    /**
     * 占比-近视
     */
    private Integer myopiaProportion;

    /**
     * 低度近视人数
     */
    private Integer lowMyopiaCount;

    /**
     * 低度近视率
     */
    private Integer lowMyopiaProportion;

    /**
     * 高度近视人数
     */
    private Integer highMyopiaCount;

    /**
     * 高度近视率
     */
    private Integer highMyopiaProportion;

    /**
     * 人数-建议就诊
     */
    private CountAndProportion recommendDoctorCount;
    /**
     * 占比-建议就诊
     */
    private CountAndProportion recommendDoctorProportion;

    /**
     * 人数-欠矫未矫
     */
    private CountAndProportion oweCount;

    /**
     * 占比-欠矫未矫
     */
    private CountAndProportion oweProportion;
}
