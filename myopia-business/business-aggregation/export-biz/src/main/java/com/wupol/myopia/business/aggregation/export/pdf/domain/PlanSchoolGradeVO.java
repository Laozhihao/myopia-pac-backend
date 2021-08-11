package com.wupol.myopia.business.aggregation.export.pdf.domain;

import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TODO:
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PlanSchoolGradeVO {

    private Integer id;

    private String gradeName;

    private List<SchoolClass> classes;
}
