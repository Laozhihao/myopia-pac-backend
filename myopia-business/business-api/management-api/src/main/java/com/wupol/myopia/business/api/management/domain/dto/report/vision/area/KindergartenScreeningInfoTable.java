package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 幼儿园筛查表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class KindergartenScreeningInfoTable {

    /**
     * 项目
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 人数-视力低常
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
     * 人数-远视储备不足
     */
    private String insufficientCount;
    /**
     * 占比-远视储备不足
     */
    private String insufficientProportion;

    /**
     * 人数-屈光不正
     */
    private String refractiveErrorCount;
    /**
     * 占比-屈光不正
     */
    private String refractiveErrorProportion;

    /**
     * 人数-屈光参差
     */
    private String anisometropiaCount;
    /**
     * 占比-屈光参差率
     */
    private String anisometropiaProportion;

    /**
     * 人数-建议就诊
     */
    private CountAndProportion recommendDoctorCount;
    /**
     * 占比-建议就诊
     */
    private CountAndProportion recommendDoctorProportion;
}
