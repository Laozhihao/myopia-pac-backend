package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 各学校筛查情况
 *
 * @author hang.yuan 2022/5/16 18:32
 */
@Data
public class DistrictSchoolScreeningMonitorVO {
    /**
     * 说明变量
     */
    private SchoolScreeningMonitorVariableVO schoolScreeningMonitorVariableVO;
    /**
     * 表格数据
     */
    private List<SchoolScreeningMonitorTable> schoolScreeningMonitorTableList;

    @Data
    public static class SchoolScreeningMonitorVariableVO{

        /**
         * 龋患率极值
         */
        private SchoolRatioExtremum saprodontiaRatioExtremum;

        /**
         * 龋患（失、补）率极值
         */
        private SchoolRatioExtremum saprodontiaLossAndRepairRatioExtremum;

        /**
         * 超重率极值
         */
        private SchoolRatioExtremum overweightRatioExtremum;
        /**
         * 肥胖率极值
         */
        private SchoolRatioExtremum obeseRatioExtremum;

        /**
         * 营养不良率极值
         */
        private SchoolRatioExtremum malnourishedRatioExtremum;

        /**
         * 生长迟缓率极值
         */
        private SchoolRatioExtremum stuntingRatioExtremum;

        /**
         * 血压偏高率极值
         */
        private SchoolRatioExtremum highBloodPressureRatioExtremum;

        /**
         * 脊柱弯曲异常率极值
         */
        private SchoolRatioExtremum abnormalSpineCurvatureRatioExtremum;

    }

    @Data
    public static class SchoolRatioExtremum{
        /**
         * 学校
         */
        private String schoolName;
        /**
         * 最高占比
         */
        private String maxRatio;
        /**
         * 最低占比
         */
        private String minRatio;
    }

    @Data
    public static class SchoolScreeningMonitorTable {

        /**
         * 项目 （性别、学龄段、年龄段）
         */
        private String itemName;

        /**
         * 筛查人数(有效数据)
         */
        private Integer validScreeningNum;

        /**
         * 龋齿数据
         */
        private DistrictCommonDiseasesAnalysisVO.SaprodontiaVO saprodontiaVO;
        /**
         * 身高体重数据
         */
        private DistrictCommonDiseasesAnalysisVO.HeightAndWeightVO heightAndWeightVO;
        /**
         * 血压与脊柱弯曲数据
         */
        private DistrictCommonDiseasesAnalysisVO.BloodPressureAndSpinalCurvatureVO bloodPressureAndSpinalCurvatureVO;

    }
}
