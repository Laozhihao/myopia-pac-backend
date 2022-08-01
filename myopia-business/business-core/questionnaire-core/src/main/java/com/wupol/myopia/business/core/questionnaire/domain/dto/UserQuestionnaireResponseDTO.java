package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户问卷DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class UserQuestionnaireResponseDTO {

    /**
     * 问卷Id
     */
    private Integer id;

    /**
     * 标题
     */
    private String title;

    /**
     * 主标题
     */
    private String mainTitle;
}
