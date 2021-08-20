package com.wupol.myopia.business.aggregation.export.pdf.domain;

import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 学校班级-年级
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PlanSchoolGradeVO {

    /**
     * 年级Id
     */
    private Integer id;

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级信息
     */
    private List<SchoolClass> classes;
}
