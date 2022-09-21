package com.wupol.myopia.business.api.school.management.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 导出学生档案卡
 *
 * @author hang.yuan 2022/9/21 12:12
 */
@Data
public class StudentArchiveCardDTO implements Serializable {

    /**
     * 学校ID
     */
    @NotNull(message = "学校ID不能为空")
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
     * 筛查学生ID
     */
    private List<Integer> planStudentId;
}
