package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.service.report.DistrictCommonDiseaseReportService;
import com.wupol.myopia.business.api.management.service.report.SchoolCommonDiseaseReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 常见病报告
 *
 * @author hang.yuan 2022/5/19 14:09
 */
@Service
@Slf4j
public class CommonDiseaseReportService {

    @Autowired
    private DistrictCommonDiseaseReportService districtCommonDiseaseReportService;
    @Autowired
    private SchoolCommonDiseaseReportService schoolCommonDiseaseReportService;

    public Callable<DistrictCommonDiseaseReportVO> districtCommonDiseaseReport(Integer districtId, Integer noticeId){
        return () -> districtCommonDiseaseReportService.districtCommonDiseaseReport(districtId,noticeId);
    }

    public SchoolCommonDiseaseReportVO schoolCommonDiseaseReport(Integer schoolId,Integer noticeId,Integer planId){
        if (Objects.nonNull(noticeId)){
            return schoolCommonDiseaseReportService.schoolCommonDiseaseReport(schoolId,planId);
        }

        if (Objects.nonNull(planId)){
            return schoolCommonDiseaseReportService.schoolCommonDiseaseReport(schoolId,planId);
        }

        throw new BusinessException("筛查通知ID或筛查计划ID不能为空");
    }
}
