package com.wupol.myopia.business.aggregation.screening.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 导出学生pdf业务实体
 *
 * @author hang.yuan 2022/9/27 10:07
 */
@Data
public class GeneratorPdfDTO implements Serializable {
    /**
     * 计划ID
     */
    @NotNull(message = "计划Id不能为空")
    private Integer planId;
    /**
     * 学校ID
     */
    private Integer schoolId;
    /**
     * 年级ID
     */
    private Integer gradeId;
    /**
     * 班级ID
     */
    private Integer classId;
    /**
     * 机构ID
     */
    private Integer orgId;
    /**
     * 筛查学生ID集合
     */
    private String planStudentIdStr;
    /**
     * 是否学校端
     */
    private Boolean isSchoolClient;
    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 筛查学生名称
     */
    private String planStudentName;
    /**
     * 是否有数据
     */
    private Boolean isData;

}
