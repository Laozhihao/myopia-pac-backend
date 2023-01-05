package com.wupol.myopia.business.api.management.service.report.refactor;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenRefractiveSituationDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenVisionReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 幼儿园视力报告
 *
 * @Author wulizhou
 * @Date 2023/1/4 12:29
 */
@Service
@Slf4j
public class KindergartenVisionReportService {

    public KindergartenVisionReportDTO kindergartenSchoolVisionReport(Integer planId, Integer schoolId) {
        return new KindergartenVisionReportDTO();
    }




}
