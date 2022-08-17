package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 问卷返回
 *
 * @author Simple4H
 */
@Getter
@Setter
public class QuestionnaireResponseDTO {

    /**
     * Id
     */
    private Integer id;

    /**
     * 标题
     */
    private String title;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 详情
     */
    private List<QuestionnaireInfoDTO> detail;
}
