package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 身高体重监测-不同学龄
 *
 * @author hang.yuan
 * @date 2022/6/8
 */
@Data
public class HeightAndWeightSchoolAge implements SchoolAgeRatioVO {

    /**
     * 超重率
     */
    private String overweightRatio;
    /**
     * 肥胖率
     */
    private String obeseRatio;

    /**
     * 营养不良率
     */
    private String malnourishedRatio;

    /**
     * 生长迟缓率
     */
    private String stuntingRatio;

    /**
     * 最高年级超重率
     */
    private GradeRatio maxOverweightRatio;
    /**
     * 最高年级肥胖率
     */
    private GradeRatio maxObeseRatio;

    /**
     * 最高年级营养不良率
     */
    private GradeRatio maxMalnourishedRatio;

    /**
     * 最高年级生长迟缓率
     */
    private GradeRatio maxStuntingRatio;

    @Override
    public Integer type() {
        return 2;
    }
}