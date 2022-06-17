package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Author HaoHao
 * @Date 2022/6/1
 **/
@Data
public class CommonDiseasePlanStudent {
    /**
     * 主键id（筛查计划学生ID）
     */
    private Integer id;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 年级ID
     */
    private Integer gradeId;

    /**
     * 学校所处区域id
     */
    private Integer schoolDistrictId;

    /**
     * 学生id
     */
    private Integer studentId;

    /**
     * 筛查计划开始时间
     */
    private Date planStartTime;

    /**
     * 筛查计划ID
     */
    private Integer planId;
}
