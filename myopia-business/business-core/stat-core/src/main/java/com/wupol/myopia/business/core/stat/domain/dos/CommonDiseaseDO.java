package com.wupol.myopia.business.core.stat.domain.dos;

import lombok.Data;

import java.io.Serializable;

/**
 * 常见病分析
 *
 * @author hang.yuan 2022/4/13 15:36
 */
@Data
public class CommonDiseaseDO implements Serializable {
    /**
     * 小学及以上--超重人数（默认0）
     */
    private Integer overweightNum;

    /**
     * 小学及以上--超重率
     */
    private String overweightRatio;

    /**
     * 小学及以上--肥胖人数（默认0）
     */
    private Integer obeseNum;

    /**
     * 小学及以上--肥胖率
     */
    private String obeseRatio;

    /**
     * 小学及以上--营养不良人数（默认0）
     */
    private Integer malnourishedNum;

    /**
     * 小学及以上--营养不良率
     */
    private String malnourishedRatio;

    /**
     * 小学及以上--生长迟缓人数（默认0）
     */
    private Integer growthRetardationNum;

    /**
     * 小学及以上--生长迟缓率
     */
    private String growthRetardationRatio;

    /**
     * 小学及以上--脊柱弯曲异常人数（默认0）
     */
    private Integer abnormalSpineCurvatureNum;

    /**
     * 小学及以上--脊柱弯曲异常率
     */
    private String abnormalSpineCurvatureRatio;

    /**
     * 小学及以上--血压偏高人数（默认0）
     */
    private Integer highBloodPressureNum;

    /**
     * 小学及以上--血压偏高率
     */
    private String highBloodPressureRatio;

    /**
     * 小学及以上--复查学生人数（默认0）
     */
    private Integer reviewStudentNum;

    /**
     * 小学及以上--复查学生率
     */
    private String reviewStudentRatio;
}
