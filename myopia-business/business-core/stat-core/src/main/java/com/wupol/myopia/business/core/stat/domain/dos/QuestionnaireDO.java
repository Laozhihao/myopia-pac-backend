package com.wupol.myopia.business.core.stat.domain.dos;

import lombok.Data;

import java.io.Serializable;

/**
 * 问卷情况
 *
 * @author hang.yuan 2022/4/13 15:38
 */
@Data
public class QuestionnaireDO implements Serializable {

    /**
     * 小学及以上--校环境健康影响因素调查表数（默认0）
     */
    private Integer envHealthInfluenceQuestionnaireNum;

    /**
     * 小学及以上--校环境健康影响因素调查表率
     */
    private String envHealthInfluenceQuestionnaireRatio;

    /**
     * 小学及以上--学校卫生工作基本情况调查表数【卫生行政部门填写】（默认0）
     */
    private Integer schoolHealthWorkAdministrativeQuestionnaireNum;

    /**
     * 小学及以上--学校卫生工作基本情况调查表率【卫生行政部门填写】
     */
    private String schoolHealthWorkAdministrativeQuestionnaireRatio;

    /**
     * 小学及以上--学校卫生工作基本情况调查表数【学校填写】（默认0）
     */
    private Integer schoolHealthWorkQuestionnaireNum;

    /**
     * 小学及以上--学校卫生工作基本情况调查表率【学校填写】
     */
    private String schoolHealthWorkQuestionnaireRatio;

    /**
     * 小学及以上--学生视力不良及脊柱弯曲异常影响因素专项调查表数（默认0）
     */
    private Integer poorVisionAndAbnormalCurvatureSpineQuestionnaireNum;

    /**
     * 小学及以上--学生视力不良及脊柱弯曲异常影响因素专项调查表率
     */
    private String poorVisionAndAbnormalCurvatureSpineQuestionnaireRatio;

    /**
     * 学生健康状况及影响因素调查表数（默认0）
     */
    private Integer healthStateQuestionnaireNum;

    /**
     * 学生健康状况及影响因素调查表率
     */
    private String healthStateQuestionnaireRatio;
}
