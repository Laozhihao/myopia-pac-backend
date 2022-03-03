package com.wupol.myopia.business.aggregation.export.excel.domain;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.Getter;
import lombok.Setter;

/**
 * 解除绑定学生
 *
 * @author Simple4H
 */
@Getter
@Setter
public class UnbindScreeningStudentDTO {

    private String idCard;

    private String passport;

    private ScreeningPlanSchoolStudent screeningPlanSchoolStudent;

    public UnbindScreeningStudentDTO(String idCard, String passport, ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        this.idCard = idCard;
        this.passport = passport;
        this.screeningPlanSchoolStudent = screeningPlanSchoolStudent;
    }
}
