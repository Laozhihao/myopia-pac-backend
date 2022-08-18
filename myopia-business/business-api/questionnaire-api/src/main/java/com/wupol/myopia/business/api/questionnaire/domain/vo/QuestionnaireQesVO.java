package com.wupol.myopia.business.api.questionnaire.domain.vo;

import lombok.Data;

/**
 * 问卷qes响应实体
 * @author hang.yuan
 * @date 2022/8/5
 */
@Data
public class QuestionnaireQesVO {

    /**
     * qes管理ID
     */
    private Integer id;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 问卷模板名称
     */
    private String name;

    /**
     * 问卷描述
     */
    private String description;

    /**
     * 预览url
     */
    private String previewUrl;
    /**
     * 是否存在qes文件
     */
    private Boolean isExistQes;
}
