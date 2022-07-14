package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;

/**
 * 问卷学校填写的情况
 *
 * @author xz 2022 07 06 12:30
 */
@Data
public class QuestionSchoolVO {
    /**
     * 学校填写个数
     */
    private int schoolAmount;

    /**
     * 学校填写完成个数
     */
    private int schoolAccomplish;

    /**
     * 学生专项填写个数
     */
    private int studentSpecialAmount;

    /**
     * 学生专项填写完成个数
     */
    private int studentSpecialAccomplish;

    /**
     * 学生环境填写个数
     */
    private int studentEnvironmentAmount;

    /**
     * 学生环境填写完成个数
     */
    private int studentEnvironmentAccomplish;
}
