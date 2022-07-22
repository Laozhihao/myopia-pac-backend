package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 筛查计划学校里的问卷年级
 * @author xz
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class GradeQuestionnaireInfo {
    /**
     * 年级id
     */
    private Integer gradeId;

    private String gradeName;

    /**
     * 有数据的学生
     */
    private Integer studentCount;
}
