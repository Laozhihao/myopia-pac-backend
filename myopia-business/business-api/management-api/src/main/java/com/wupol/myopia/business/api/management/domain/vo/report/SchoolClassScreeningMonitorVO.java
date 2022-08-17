package com.wupol.myopia.business.api.management.domain.vo.report;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

import java.util.List;

/**
 * 各班级筛查情况
 *
 * @author hang.yuan 2022/5/16 18:32
 */
@Data
public class SchoolClassScreeningMonitorVO {

    /**
     * 年级
     */
    private String grade;

    /**
     * 班级说明
     */
    private SchoolClassScreeningMonitorVariableVO schoolClassScreeningMonitorVariableVO;
    /**
     * 班级表格数据
     */
    private List<ScreeningMonitorTable> schoolClassScreeningMonitorTableList;
    /**
     * 班级图表数据
     */
    private List<ChartVO.GradeRatioExtremumChart> schoolClassScreeningMonitorChart;

    public Boolean notEmpty(){
        return CollectionUtil.isNotEmpty(schoolClassScreeningMonitorTableList);
    }

    @Data
    public static class SchoolClassScreeningMonitorVariableVO{


        /**
         * 龋患率极值
         */
        private GradeRatioExtremum saprodontiaRatioExtremum;

        /**
         * 龋患（失、补）率极值
         */
        private GradeRatioExtremum saprodontiaLossAndRepairRatioExtremum;

        /**
         * 超重率极值
         */
        private GradeRatioExtremum overweightRatioExtremum;
        /**
         * 肥胖率极值
         */
        private GradeRatioExtremum obeseRatioExtremum;

        /**
         * 营养不良率极值
         */
        private GradeRatioExtremum malnourishedRatioExtremum;

        /**
         * 生长迟缓率极值
         */
        private GradeRatioExtremum stuntingRatioExtremum;

        /**
         * 血压偏高率极值
         */
        private GradeRatioExtremum highBloodPressureRatioExtremum;

        /**
         * 脊柱弯曲异常率极值
         */
        private GradeRatioExtremum abnormalSpineCurvatureRatioExtremum;

    }

}
