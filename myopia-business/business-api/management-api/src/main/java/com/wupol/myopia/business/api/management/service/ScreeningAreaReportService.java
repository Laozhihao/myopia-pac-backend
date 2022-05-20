package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.ScreeningAreaReportDTO;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 筛查报告-区域
 *
 * @author Simple4H
 */
@Service
public class ScreeningAreaReportService {

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private DistrictService districtService;

    public ScreeningAreaReportDTO generateReport(Integer noticeId, Integer planId, Integer districtId) {
        Set<Integer> childDistrictIds;
        try {
            childDistrictIds = districtService.getChildDistrictIdsByDistrictId(districtId);
        } catch (IOException e) {
            throw new BusinessException("获取区域异常");
        }
        List<StatConclusion> statConclusions = statConclusionService.getByNoticePlanDistrict(noticeId, planId, childDistrictIds);
        return new ScreeningAreaReportDTO();
    }

    private void generateAreaReportInfo(List<StatConclusion> statConclusions,Integer districtId) {
        String districtNameByDistrictId = districtService.getDistrictNameByDistrictId(districtId);
    }

}
