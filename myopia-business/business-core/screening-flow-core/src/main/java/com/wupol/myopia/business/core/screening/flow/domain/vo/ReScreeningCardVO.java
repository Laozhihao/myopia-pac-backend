package com.wupol.myopia.business.core.screening.flow.domain.vo;

import com.wupol.myopia.business.core.screening.flow.domain.dos.DeviationDO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/*
 * @Author  钓猫的小鱼
 * @Date  2022/4/13 20:45
 * @Email: shuailong.wu@vistel.cnø
 * @Des: 复测卡扩展类
 */

@Data
public class ReScreeningCardVO {
    /**
     * 常见病编码
     */
    private String commonDiseasesCode;

    /**
     * 视力筛查
     */
    private Vision vision;
    /**
     * 常见病筛查
     */
    private CommonDiseases commonDiseases;

    @Data
    public static class Vision implements Serializable{
        /**
         * 佩戴眼镜的类型： {@link com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation}
         */
        private Integer glassesType;
        /**
         * 视力检查结果
         */
        private VisionResult visionResult;
        /**
         * 自动电脑验光检查结果
         */
        private ComputerOptometryResult computerOptometryResult;
        /**
         * 视力或屈光检查误差误差
         */
        private DeviationDO.VisionOrOptometryDeviation visionOrOptometryDeviation;
        /**
         * 质控人员
         */
        private String qualityControlName;
        /**
         * 创建时间
         */
        private Date createTime;

        /**
         * 视力检查结果
         */
        @Data
        public static class VisionResult implements Serializable{
            /**
             * 右眼视力
             */
            private VisionData rightEyeData;
            /**
             * 左眼视力
             */
            private VisionData leftEyeData;

            @Data
            public static class VisionData implements Serializable{

                /**
                 * 裸眼视力
                 */
                private BigDecimal nakedVision;
                /**
                 * 裸眼视力-复测
                 */
                private BigDecimal nakedVisionRetest;
                /**
                 * 裸眼视力-差值
                 */
                private BigDecimal nakedVisionDeviation;

                /**
                 * 矫正视力
                 */
                private BigDecimal correctedVision;
                /**
                 * 矫正视力-复测
                 */
                private BigDecimal correctedVisionRetest;
                /**
                 * 矫正视力-差值
                 */
                private BigDecimal correctedVisionDeviation;
            }

        }


        /**
         * 自动电脑验光检查结果
         */
        @Data
        public static class ComputerOptometryResult implements Serializable{
            /**
             * 球镜(右眼)
             */
            private BigDecimal rightSE;
            /**
             * 球镜(右眼)-复测
             */
            private BigDecimal rightSERetest;
            /**
             * 球镜(右眼)-差值
             */
            private BigDecimal rightSEDeviation;

            /**
             * 球镜(左眼)
             */
            private BigDecimal leftSE;
            /**
             * 球镜(左眼)-复测
             */
            private BigDecimal leftSERetest;
            /**
             * 球镜(左眼)-复测
             */
            private BigDecimal leftSEDeviation;

        }
    }
    @Data
    public static class CommonDiseases implements Serializable{
        /**
         * 身高和体格检查误差卡片
         */
        private HeightAndWeightResult heightAndWeightResult;

        /**
         * 质控人员
         */
        private String qualityControlName;
        /**
         * 创建时间
         */
        private Date createTime;

        /**
         * 身高和体格检查误差卡片
         */
        @Data
        public static class HeightAndWeightResult implements Serializable{
            /**
             * 身高
             */
            private BigDecimal height;
            /**
             * 身高-复测
             */
            private BigDecimal heightRescreen;
            /**
             * 身高-误差
             */
            private BigDecimal heightDeviation;

            /**
             * 身高-误差原因
             */
            private String heightDeviationRemark;

            /**
             * 体重
             */
            private BigDecimal weight;
            /**
             * 体重-复测
             */
            private BigDecimal weightRescreen;
            /**
             * 体重-误差
             */
            private BigDecimal weightDeviation;

            /**
             * 体重-误差原因
             */
            private String weightDeviationRemark;

        }
    }

}
