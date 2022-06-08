package com.wupol.myopia.business.api.management.service.report;

import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 报告图表
 *
 * @author hang.yuan 2022/6/4 00:56
 */
@Service
public class ReportChartService {

    /**
     * 不同性别-图表
     */
    public void getSexMonitorChart(List<StatConclusion> statConclusionList, SexChartVO sexChartVO) {
        ReportSexChart.getSexMonitorChart(statConclusionList,sexChartVO);
    }


    /**
     *  不同年龄段-图表
     */
    public void getAgeMonitorChart(List<StatConclusion> statConclusionList, AgeChartVO ageChartVO) {
        ReportAgeChart.getAgeMonitorChart(statConclusionList,ageChartVO);
    }


    /**
     *  不同学龄-图表
     */
    public void getSchoolAgeMonitorChart(List<TwoTuple<String, SchoolAgeRatioVO>> tupleList, SchoolAgeChartVO schoolAgeChartVO){
        ReportSchoolAgeChart.getSchoolAgeMonitorChart(tupleList, schoolAgeChartVO);
    }


    /**
     * 不同班级
     */
    public void getGradeMonitorChart(List<StatConclusion> statConclusionList, GradeChartVO gradeChartVO) {
        ReportGradeChart.getGradeMonitorChart(statConclusionList,gradeChartVO);
    }

}
