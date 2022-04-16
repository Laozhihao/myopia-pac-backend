package com.wupol.myopia.business.core.screening.flow.domain.dto;/*
 * @Author  钓猫的小鱼
 * @Date  2022/4/12 16:54
 * @Email: shuailong.wu@vistel.cn
 * @Des:
 */

import com.amazonaws.services.dynamodbv2.xspec.B;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 筛查信息扩展类：包括视力筛查/常见病筛查/复测筛查
 */
@Data
public class ScreeningInfoDTO {
    /**
     * 视力信息
     */
    private List<StudentResultDetailsDTO> vision;
    /**
     * 常见病
     */
    private CommonDiseases commonDiseases;
    /**
     * 复测信息
     */
    public Rescreening rescreening;

    /**
     * 复测
     */
    @Data
    public static class Rescreening implements Serializable{
        /**
         * 复测次数
         */
        private Integer doubleCount;
        /**
         * 错误次数
         */
        private Integer deviationCount;
        /**
         * 复测内容（计算后）
         */
        private ReScreeningResult rescreeningResult;
        /**
         * 误差说明
         */
        private DeviationDO deviation;

        /**
         * 计算后内容（复测）
         */
        @Data
        public static class ReScreeningResult implements Serializable{
            /** 戴镜情况 */
            private String glassesTypeDesc;

            /** 裸眼（右） */
            private ScreeningDeviation rightNakedVision;

            /** 矫正（右） */
            private ScreeningDeviation rightCorrectedVision;

            /** 球镜（右） */
            private ScreeningDeviation rightSph;

            /** 柱镜（右） */
            private ScreeningDeviation rightCyl;

            /** 轴位（右） */
            private ScreeningDeviation rightAxial;

            /** 裸眼（左） */
            private ScreeningDeviation leftNakedVision;

            /** 矫正（左） */
            private ScreeningDeviation leftCorrectedVision;

            /** 球镜（左） */
            private ScreeningDeviation lefttSph;

            /** 柱镜（左） */
            private ScreeningDeviation lefttCyl;

            /** 轴位（左） */
            private ScreeningDeviation leftAxial;



            /** 身高 */
            private ScreeningDeviation height;

            /** 体重 */
            private ScreeningDeviation weight;

            @Data
            public static class ScreeningDeviation implements Serializable {
                /**
                 *
                 */
                private BigDecimal content;
                /**
                 * 是否错误
                 */
                private int type;
            }

        }
    }

    /**
     * 常见病
     */
    @Data
    public static class CommonDiseases implements Serializable {
        /**
         * 筛查结果--龋齿
         */
        private SaprodontiaDataDO saprodontiaData;

        /**
         * 筛查结果--脊柱
         */
        private SpineDataDO spineData;

        /**
         * 筛查结果--血压
         */
        private BloodPressureDataDO bloodPressureData;

        /**
         * 筛查结果--疾病史(汉字)
         */
        private List<String> diseasesHistoryData;
        /**
         * 筛查结果--隐私项
         */
        private PrivacyDataDO privacyData;

        /**
         * 筛查结果--全身疾病在眼部的表现
         */
        private String systemicDiseaseSymptom;
        /**
         * 筛查结果--身高体重
         */
        private HeightAndWeightDataDO heightAndWeightData;
    }


}
