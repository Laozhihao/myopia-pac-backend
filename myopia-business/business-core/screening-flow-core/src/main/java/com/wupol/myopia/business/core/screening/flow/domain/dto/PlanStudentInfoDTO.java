package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 筛查学生
 *
 * @author Simple4H
 */
@Accessors(chain = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
public class PlanStudentInfoDTO extends ScreeningPlanSchoolStudent {

    /**
     * 筛查计划--年级名称
     */
    private String gradeName;

    /**
     * 筛查计划--年级名称
     */
    private String className;

    /**
     * 学号
     */
    private String sno;

}
