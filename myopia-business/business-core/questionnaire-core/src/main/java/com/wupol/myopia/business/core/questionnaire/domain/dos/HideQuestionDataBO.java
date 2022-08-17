package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Data;

/**
 * 隐藏问题数据实体
 *
 * @author hang.yuan 2022/7/27 13:47
 */
@Data
public class HideQuestionDataBO {

    /**
     * 问题ID
     */
    private Integer questionId;

    /**
     * ID
     */
    private String commonDiseaseId;
    /**
     * 省（市/自治区）
     */
    private String provinceCode;
    /**
     * 地市（州）
     */
    private String cityCode;

    /**
     * 片区
     */
    private Integer areaType;

    /**
     * 区（县）
     */
    private String areaCode;

    /**
     * 监测点
     */
    private Integer monitorType;

    /**
     * 学校名称（盖章）
     */
    private String schoolName;

    /**
     * 填表日期
     */
    private String fillDate;

    public HideQuestionDataBO(Integer questionId) {
        this.questionId = questionId;
    }

    public HideQuestionDataBO() {
    }
}
