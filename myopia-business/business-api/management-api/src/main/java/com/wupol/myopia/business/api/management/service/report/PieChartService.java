package com.wupol.myopia.business.api.management.service.report;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.PieChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.PieChartDetail;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.LowVisionLevelEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 饼图
 *
 * @author Simple4H
 */
@Service
public class PieChartService {

    @Resource
    private CountAndProportionService countAndProportionService;

    public PieChart genderLowVisionChart(List<StatConclusion> statConclusions, Long total) {
        return getPieChart(statConclusions, total);
    }

    public PieChart areaGenderPieChart(List<StatConclusion> statConclusions, Long total) {
        return getPieChart(statConclusions, total);
    }

    public PieChart levelLowVisionChart(List<StatConclusion> statConclusions, Long total) {
        if (CollectionUtils.isEmpty(statConclusions)) {
            return null;
        }
        return new PieChart(countAndProportionService.lowVision(statConclusions, total).getProportion(),
                LowVisionLevelEnum.lowVisionLevelList().stream().map(lowVisionLevel ->
                        new PieChartDetail(lowVisionLevel.desc, countAndProportionService.levelLowVision(statConclusions, lowVisionLevel.code).getProportion())).collect(Collectors.toList()));
    }

    private PieChart getPieChart(List<StatConclusion> statConclusions, Long total) {
        if (CollectionUtils.isEmpty(statConclusions)) {
            return null;
        }
        return new PieChart(countAndProportionService.lowVision(statConclusions, total).getProportion(),
                GenderEnum.genderList().stream().map(gender -> new PieChartDetail(gender.cnDesc, countAndProportionService.genderLowVision(statConclusions, gender.type).getProportion())).collect(Collectors.toList()));
    }
}
