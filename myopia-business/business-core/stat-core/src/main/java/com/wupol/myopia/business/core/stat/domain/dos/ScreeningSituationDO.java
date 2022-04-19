package com.wupol.myopia.business.core.stat.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 筛查情况
 *
 * @author hang.yuan 2022/4/13 16:43
 */
@Data
@Accessors(chain = true)
public class ScreeningSituationDO implements Serializable {
    /**
     * 学校数
     */
    private Integer schoolNum;

    /**
     * 计划的学生数量（默认0）
     */
    private Integer planScreeningNum;

    /**
     * 实际筛查的学生数量（默认0）
     */
    private Integer realScreeningNum;

    /**
     * 完成率
     */
    private String finishRatio;

    /**
     * 纳入统计的实际筛查学生数量（默认0）
     */
    private Integer validScreeningNum;

    /**
     * 纳入统计的实际筛查学生比例
     */
    private String validScreeningRatio;
}
