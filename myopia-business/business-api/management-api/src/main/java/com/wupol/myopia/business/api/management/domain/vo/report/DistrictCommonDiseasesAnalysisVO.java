package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 按区域常见病分析
 *
 * @author hang.yuan 2022/5/16 16:07
 */
@Data
public class DistrictCommonDiseasesAnalysisVO {

    /**
     * 常见病分析变量
     */
    private CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO;

    /**
     * 疾病监测情况
     */
    private DistrictDiseaseMonitorVO districtDiseaseMonitorVO;

    /**
     * 龋齿监测结果
     */
    private DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO;

    /**
     * 体重身高监测结果
     */
    private DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO;

    /**
     * 血压与脊柱弯曲异常监测结果
     */
    private DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO;

    /**
     * 各学校筛查情况
     */
    private DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO;


}
