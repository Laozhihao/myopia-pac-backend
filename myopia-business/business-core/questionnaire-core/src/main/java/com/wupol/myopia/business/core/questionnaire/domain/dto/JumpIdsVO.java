package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 跳转题目
 *
 * @author Simple4H
 */
@Getter
@Setter
public class JumpIdsVO {

    /**
     * 选项Id
     */
    private Integer optionId;

    /**
     * 跳转题目Id
     */
    private List<Integer> jumpIds;
}
