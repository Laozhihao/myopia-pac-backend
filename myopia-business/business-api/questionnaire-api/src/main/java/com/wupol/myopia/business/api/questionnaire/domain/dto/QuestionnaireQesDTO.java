package com.wupol.myopia.business.api.questionnaire.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 问卷qes接收实体
 *
 * @author hang.yuan 2022/8/4 23:47
 */
@Data
public class QuestionnaireQesDTO {

    /**
     * 问卷模板qes管理ID
     */
    private Integer id;
    /**
     * 年份
     */
    @NotNull(message = "年份不能为空")
    private Integer year;
    /**
     * 问卷模板名称
     */
    @NotNull(message = "问卷模板名称不能为空")
    private String name;

    /**
     * 问卷描述
     */
    private String description;

    /**
     * 区域ID
     */
    private Integer districtId;
}
