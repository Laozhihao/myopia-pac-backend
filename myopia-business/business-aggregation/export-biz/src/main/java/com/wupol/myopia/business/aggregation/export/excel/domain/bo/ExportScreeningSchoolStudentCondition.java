package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 导出筛查学校学生的条件
 *
 * @author hang.yuan 2022/7/7 12:09
 */
@Accessors(chain = true)
@Data
public class ExportScreeningSchoolStudentCondition implements Serializable {


    /**
     * 筛查计划ID
     */
    private Integer screeningPlanId;
    /**
     * 学生ID
     */
    private Integer schoolId;
    /**
     * 原文件名称
     */
    private String fileName;

    /**
     * 用户ID
     */
    private Integer userId;

}
