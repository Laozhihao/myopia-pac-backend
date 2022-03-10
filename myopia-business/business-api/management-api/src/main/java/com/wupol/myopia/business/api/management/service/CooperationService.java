package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author wulizhou
 * @Date 2022/1/12 10:05
 */
@Service
public class CooperationService {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    @Autowired
    private OverviewService overviewService;

    /**
     * 更新医院状态
     * @return
     */
    public int handleHospitalStatus(Date date) {
        List<Hospital> hospitals = hospitalService.getUnhandleHospital(date);
        int result = 0;
        for (Hospital hospital : hospitals) {
            result += hospitalService.updateHospitalStatus(hospital.getId(), hospital.getCooperationStopStatus(), hospital.getStatus());
        }
        return result;
    }

    /**
     * 更新学校状态
     * @return
     */
    public int handleSchoolStatus(Date date) {
        List<School> schools = schoolService.getUnhandleSchool(date);
        int result = 0;
        for (School school : schools) {
            result += schoolService.updateSchoolStatus(school.getId(), school.getCooperationStopStatus(), school.getStatus());
        }
        return result;
    }

    /**
     * 更新筛查机构状态
     * @return
     */
    public int handleOrganizationStatus(Date date) {
        List<ScreeningOrganization> orgs = screeningOrganizationService.getUnhandleOrganization(date);
        int result = 0;
        for (ScreeningOrganization org : orgs) {
            result += screeningOrganizationService.updateOrganizationStatus(org.getId(), org.getCooperationStopStatus(), org.getStatus());
        }
        return result;
    }

    /**
     * 更新总览机构状态
     * @param date
     * @return
     */
    public int handleOverviewStatus(Date date) {
        List<Overview> overviews = overviewService.getUnhandleOverview(date);
        int result = 0;
        for (Overview overview : overviews) {
            result += overviewService.updateOverviewStatus(overview.getId(), overview.getCooperationStopStatus(), overview.getStatus());
        }
        return result;
    }

}
