package com.wupol.myopia.business.aggregation.student.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 年级信息
 *
 * @author hang.yuan 2022/9/13 14:33
 */
@Data
@Accessors(chain = true)
public class GradeInfoVO implements Serializable {
    /**
     * 年级ID
     */
    private Integer gradeId;
    /**
     * 年级名称
     */
    private String gradeName;
    /**
     * 学生总数
     */
    private Integer studentNum;
    /**
     * 未同步到计划学生数
     */
    private Integer unSyncStudentNum;
}
