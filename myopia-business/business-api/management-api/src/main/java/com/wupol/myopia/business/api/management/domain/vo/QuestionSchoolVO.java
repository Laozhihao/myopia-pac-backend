package com.wupol.myopia.business.api.management.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 问卷学校填写的情况
 *
 * @author xz 2022 07 06 12:30
 */
@Data
@AllArgsConstructor
public class QuestionSchoolVO {
    /**
     * 学校填写个数
     */
    private Integer schoolAmount;

    /**
     * 学校填写完成个数
     */
    private Integer schoolAccomplish;

    /**
     * 学生专项填写个数
     */
    private Integer studentSpecialAmount;

    /**
     * 学生专项填写完成个数
     */
    private Integer studentSpecialAccomplish;

    /**
     * 学生环境填写个数
     */
    private Integer studentEnvironmentAmount;

    /**
     * 学生环境填写完成个数
     */
    private Integer studentEnvironmentAccomplish;

    public static QuestionSchoolVO init(){
        return new QuestionSchoolVO(0,0,0,0,0,0);
    }
}
