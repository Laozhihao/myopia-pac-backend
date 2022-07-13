package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2022/07/11 18:10
 */
@Service
public class ScreeningPlanSchoolBizService {

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    /**
     * 通过姓名与证件号获取记录
     * @param schoolNo
     * @param screeningType
     * @return
     */
    public School getLastBySchoolNoAndScreeningType(String schoolNo, Integer screeningType) {
        School school = schoolService.getBySchoolNo(schoolNo);
        if (Objects.nonNull(school)) {
            ScreeningPlanSchool screeningPlanSchool = screeningPlanSchoolService.getLastBySchoolIdAndScreeningType(school.getId(), screeningType);
            return Objects.nonNull(screeningPlanSchool) ? school : null;
        }
        return null;
    }

}
