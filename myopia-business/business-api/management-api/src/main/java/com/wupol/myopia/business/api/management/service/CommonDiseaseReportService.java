package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.service.report.DistrictCommonDiseaseReportService;
import com.wupol.myopia.business.api.management.service.report.SchoolCommonDiseaseReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

/**
 * 常见病报告
 *
 * @author hang.yuan 2022/5/19 14:09
 */
@Service
public class CommonDiseaseReportService {

    @Autowired
    private DistrictCommonDiseaseReportService districtCommonDiseaseReportService;
    @Autowired
    private SchoolCommonDiseaseReportService schoolCommonDiseaseReportService;

    public DistrictCommonDiseaseReportVO districtCommonDiseaseReport(Integer districtId, Integer noticeId){
        return districtCommonDiseaseReportService.districtCommonDiseaseReport(districtId,noticeId);
    }

    public SchoolCommonDiseaseReportVO schoolCommonDiseaseReport(Integer schoolId,Integer planId){
        return schoolCommonDiseaseReportService.schoolCommonDiseaseReport(schoolId,planId);
    }
}
