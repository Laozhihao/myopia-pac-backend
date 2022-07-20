package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 搜索问题
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SearchQuestionRequestDTO {

    /**
     * 问题名称
     */
    @NotBlank(message = "标题不能为空")
    private String name;

    /**
     * 是否节点
     */
    private Boolean isTitle;
}
