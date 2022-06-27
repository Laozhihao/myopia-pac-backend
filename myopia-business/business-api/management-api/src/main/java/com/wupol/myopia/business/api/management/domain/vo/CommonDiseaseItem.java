package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;

/**
 * 常见病筛查项
 * @author hang.yuan
 * @date 2022/6/20
 */
@Data
public class CommonDiseaseItem {
    /**
     * 小学及以上--龋均人数（默认0）
     */
    private Integer dmftNum;

    /**
     * 小学及以上--龋均率
     */
    private String dmftRatio;

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