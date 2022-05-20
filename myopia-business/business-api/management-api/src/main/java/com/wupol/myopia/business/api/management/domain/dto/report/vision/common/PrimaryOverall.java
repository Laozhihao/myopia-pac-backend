package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.PrimaryScreeningInfoTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 小学及以上整体情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimaryOverall {

    /**
     * 视力低下率
     */
    private String lowVisionProportion;

    /**
     * 视力低下
     */
    private HighLowProportion lowVision;

    /**
     * 近视率
     */
    private String myopiaProportion;

    /**
     * 近视
     */
    private HighLowProportion myopia;

    /**
     * 近视前期率
     */
    private String earlyMyopiaProportion;

    /**
     * 近视前期
     */
    private HighLowProportion earlyMyopia;

    /**
     * 低度近视率
     */
    private String lightMyopiaProportion;

    /**
     * 低度近视
     */
    private HighLowProportion lightMyopia;

    /**
     * 高度近视率
     */
    private String highMyopiaProportion;

    /**
     * 高度近视
     */
    private HighLowProportion highMyopia;

    /**
     * 建议就诊率
     */
    private String recommendDoctorProportion;

    /**
     * 建议就诊
     */
    private HighLowProportion recommendDoctor;

    /**
     * 欠矫未矫
     */
    private HighLowProportion owe;

    /**
     * 表格
     */
    private List<PrimaryScreeningInfoTable> tables;
}
