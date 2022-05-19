package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.SchoolAgeGenderTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.VisionWarningSituation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 概述
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AreaOutline {

    /**
     * 信息
     */
    private Info info;

    /**
     * 学龄段性别统计表格
     */
    private List<SchoolAgeGenderTable> tables;

    /**
     * 幼儿园统计
     */
    private Kindergarten kindergarten;

    /**
     * 中小学
     */
    private Primary primary;

    /**
     * 视力预警监测情况
     */
    private VisionWarningSituation visionWarningSituation;

    /**
     * 建议就诊
     */
    private CountAndProportion recommendDoctor;


    @Getter
    @Setter
    public static class Info {
        /**
         * 区域
         */
        private String area;

        /**
         * 学校类型
         */
        private String schoolAge;

        /**
         * 学校统计
         */
        private String schoolCount;

        /**
         * 合计人数
         */
        private Integer studentTotal;

        /**
         * 本次未筛
         */
        private Integer unScreening;

        /**
         * 有效人数
         */
        private Integer validTotal;
    }


}
