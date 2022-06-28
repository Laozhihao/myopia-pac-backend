package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 按学校常见病分析
 *
 * @author hang.yuan 2022/5/16 16:07
 */
@Data
public class SchoolCommonDiseasesAnalysisVO {

    /**
     * 常见病分析变量
     */
    private CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO;


    /**
     * 龋齿监测结果
     */
    private SchoolSaprodontiaMonitorVO schoolSaprodontiaMonitorVO;

    /**
     * 体重身高监测结果
     */
    private SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO;

    /**
     * 血压与脊柱弯曲异常监测结果
     */
    private SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO;

    /**
     * 各班级筛查情况
     */
    private List<SchoolClassScreeningMonitorVO> schoolClassScreeningMonitorVOList;


}
