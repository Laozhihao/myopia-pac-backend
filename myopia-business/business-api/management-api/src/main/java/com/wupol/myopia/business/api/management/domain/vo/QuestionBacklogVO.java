package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;

/**
 * 学校填写的情况
 *
 * @author xz 2022 07 06 12:30
 */
@Data
public class QuestionBacklogVO {
    /**
     * 问卷名称
     */
    private String questionnaireTitle;

    /**
     * 总数
     */
    private int amount;

    /**
     * 完成个数
     */
    private int accomplish;

    /**
     * 问卷Id
     */
    private Integer questionnaireId;
}
