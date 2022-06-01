package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import lombok.*;

/**
 * 中小学筛查表格
 *
 * @author Simple4H
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private Long lowVisionCount;

    /**
     * 占比-视力低下/视力低常
     */
    private String lowVisionProportion;

    /**
     * 平均视力
     */
    private String avgVision;

    /**
     * 人数-近视
     */
    private Long myopiaCount;

    /**
     * 占比-近视
     */
    private String myopiaProportion;

    /**
     * 近视前期人数
     */
    private Long earlyMyopiaCount;

    /**
     * 近视前期率
     */
    private String earlyMyopiaProportion;

    /**
     * 低度近视人数
     */
    private Long lightMyopiaCount;

    /**
     * 低度近视率
     */
    private String lightMyopiaProportion;

    /**
     * 高度近视人数
     */
    private Long highMyopiaCount;

    /**
     * 高度近视率
     */
    private String highMyopiaProportion;

    /**
     * 人数-建议就诊
     */
    private Long recommendDoctorCount;
    /**
     * 占比-建议就诊
     */
    private String recommendDoctorProportion;

    /**
     * 人数-欠矫未矫
     */
    private Long oweCount;

    /**
     * 占比-欠矫未矫
     */
    private String oweProportion;
}
