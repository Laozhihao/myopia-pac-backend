package com.wupol.myopia.business.core.screening.flow.domain.dto;/*
 * @Author  钓猫的小鱼
 * @Date  2022/4/12 16:54
 * @Email: shuailong.wu@vistel.cn
 * @Des:
 */

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
    private CommonDesease commonDesease;
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
        private Integer errorCount;
        /**
         * 初筛内容(计算后)
         */
        private ScreeningResult screeningResult;
        /**
         * 复测内容（计算后）
         */
        private ScreeningResult rescreeningResult;
        /**
         * 误差说明
         */
        private DeviationDO deviationDO;
        /**
         * 复测内容
         */
        private ScreeningContent screeningContent;

        /**
         * 计算后内容
         */
        @Data
        public static class ScreeningContent implements Serializable{
            /**
             * 筛查结果--视力检查结果
             */
            private VisionDataDO visionData;

            /**
             * 筛查结果--电脑验光
             */
            private ComputerOptometryDO computerOptometry;

            /**
             * 筛查结果--身高体重
             */
            private HeightAndWeightDataDO heightAndWeightData;

        }

        /**
         * 计算后内容
         */
        @Data
        public static class ScreeningResult implements Serializable{
            /** 戴镜情况 */
            private String glassesTypeDesc;

            /** 裸眼（右/左） */
            private String nakedVisions;

            /** 矫正（右/左） */
            private String correctedVisions;

            /** 球镜（右/左） */
            private String sphs;

            /** 柱镜（右/左） */
            private String cyls;

            /** 轴位（右/左） */
            private String axials;

            /** 身高 */
            private String height;

            /** 体重 */
            private String weight;

        }
    }

    /**
     * 常见病
     */
    @Data
    public static class CommonDesease implements Serializable {
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
