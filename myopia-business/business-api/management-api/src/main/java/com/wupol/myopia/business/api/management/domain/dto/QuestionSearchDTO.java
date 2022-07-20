package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 问卷搜索
 *
 * @author xz
 */
@Getter
@Setter
public class QuestionSearchDTO {
    /**
     * 页码
     */
    private Integer current;

    /**
     * 每页显示条数
     */
    private Integer size;

    /**
     * 地区Id
     */
    private Integer areaId;

    /**
     * 任务Id
     */
    private Integer taskId;

    /**
     * 学校名搜索
     */
    private String schoolName;
}
