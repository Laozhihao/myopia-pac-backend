package com.wupol.myopia.business.core.school.management.domain.vo;

import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校学生详情
 *
 * @author hang.yuan 2022/10/13 17:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SchoolStudentVO extends SchoolStudent {

    /**
     * 学校名称
     */
    private String schoolName;
}
