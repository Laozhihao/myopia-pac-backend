package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ScreeningVisionStatisticDTO extends ScreeningBasicResult {
    /**
     * 当前级的数据
     */
    private Item totalData;
    /**
     * 当前级的数据
     */
    private Item currentData;
    /**
     * 下级的数据列表，如果没有的话，为null
     */
    private List<Item> subordinateDataList;

    @Data
    public static class Item {
        /**
         * 查看的范围(地区或者学校名）
         */
        private String screeningRangeName;
        /**
         * 筛查学生数
         */
        private Integer screeningNum;
        /**
         * 实际筛查学生数
         */
        private Integer actualScreeningNum;
        /**
         * 左眼平均视力
         */
        private BigDecimal averageVisionLeft;
        /**
         * 右眼平均视力
         */
        private BigDecimal averageVisionRight;
        /**
         * 视力低下率
         */
        private BigDecimal lowVisionRatio;
        /**
         * 视力低下人数
         */
        private Integer lowVisionNum;
        /**
         * 屈光不正率
         */
        private BigDecimal refractiveErrorRatio;
        /**
         * 屈光不正人数
         */
        private Integer refractiveErrorNum;
        /**
         * 戴镜率
         */
        private BigDecimal wearingGlassesRatio;
        /**
         * 戴镜人数
         */
        private BigDecimal wearingGlassesNum;
        /**
         * 近视人数
         */
        private BigDecimal myopiaNum;

        /**
         * 近视率
         */
        private BigDecimal myopiaRatio;

        /**
         * 0级预警率
         */
        private BigDecimal warningLevelZeroRatio;

        /**
         * 0级预警人数
         */
        private BigDecimal warningLevelZeroNum;

        /**
         * 1级预警率
         */
        private BigDecimal warningLevelOneRatio;

        /**
         * 1级预警人数
         */
        private BigDecimal warningLevelOneNum;

        /**
         * 2级预警率
         */
        private BigDecimal warningLevelTwoRatio;

        /**
         * 2级预警人数
         */
        private BigDecimal warningLevelTwoNum;

        /**
         * 3级预警率
         */
        private BigDecimal warningLevelThreeRatio;

        /**
         * 3级预警人数
         */
        private BigDecimal warningLevelThreeNum;

        /**
         * 重点视力对象数量
         */
        private Integer focusTargetsNum;

        /**
         * 建议就诊数
         */
        private Integer recommendVisitNum;


    }



    /*  *//** 视力筛查完成率 *//*
    private BigDecimal screeningFinishedRatio;

    *//** 复测人数 *//*
    private Integer rescreenNum;



    *//** 戴镜复测人数 *//*
    private Integer wearingGlassesRescreenNum;

    *//** 戴镜复测指标数 *//*
    private Integer wearingGlassesRescreenIndexNum;

    *//** 非戴镜复测人数 *//*
    private Integer withoutGlassesRescreenNum;

    *//** 非戴镜复测指标数 *//*
    private Integer withoutGlassesRescreenIndexNum;

    *//** 复测项次 *//*
    private Integer rescreenItemNum;

    *//** 错误项次数 *//*
    private Integer incorrectItemNum;

    *//** 错误率/发生率 *//*
    private BigDecimal incorrectRatio;*/
}
