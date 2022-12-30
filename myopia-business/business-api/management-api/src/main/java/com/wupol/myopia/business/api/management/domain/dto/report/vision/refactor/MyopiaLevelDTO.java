package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import com.wupol.myopia.business.common.utils.util.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 近视情况（程度）
 * @Author wulizhou
 * @Date 2022/12/26 17:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MyopiaLevelDTO {

    /**
     * 有效筛查人数
     */
    private Integer validScreeningNum;

    /**
     * 视力不良人数
     */
    private Integer lowVisionNum;

    /**
     * 视力不良率
     */
    private Float lowVisionRatio;

    /**
     * 低度近视人数
     */
    private Integer lightMyopiaNum;

    /**
     * 低度近视率
     */
    private Float lightMyopiaRatio;

    /**
     * 中度近视人数
     */
    private Integer middleMyopiaNum;

    /**
     * 中度近视率
     */
    private Float middleMyopiaRatio;

    /**
     * 高度近视人数
     */
    private Integer highMyopiaNum;

    /**
     * 高度近视率
     */
    private Float highMyopiaRatio;

    public void empty() {
        setValidScreeningNum(0);
        setLowVisionNum(0);
        setLowVisionRatio(0.0f);
        setLightMyopiaNum(0);
        setLightMyopiaRatio(0.0f);
        setMiddleMyopiaNum(0);
        setMiddleMyopiaRatio(0.0f);
        setHighMyopiaNum(0);
        setHighMyopiaRatio(0.0f);
    }

    /**
     * 生成视力情况(全局比例)
     * @param validScreeningNum
     * @param lowVisionNum
     * @param lightMyopiaNum
     * @param middleMyopiaNum
     * @param highMyopiaNum
     */
    public void generateData(int validScreeningNum, int lowVisionNum, int lightMyopiaNum, int middleMyopiaNum, int highMyopiaNum) {
        generateData(validScreeningNum, lowVisionNum, lightMyopiaNum, middleMyopiaNum, highMyopiaNum, true);
    }

    /**
     * 生成视力情况
     * @param validScreeningNum
     * @param lowVisionNum
     * @param lightMyopiaNum
     * @param middleMyopiaNum
     * @param highMyopiaNum
     * @param isGlobalRatio 是否全局占比，若为全局占比，不良率分母为有效筛查人数，否则为视力不良人数
     */
    public void generateData(int validScreeningNum, int lowVisionNum, int lightMyopiaNum, int middleMyopiaNum, int highMyopiaNum, boolean isGlobalRatio) {
        setValidScreeningNum(validScreeningNum);
        setLowVisionNum(lowVisionNum);
        setLowVisionRatio(MathUtil.divide(lowVisionNum, isGlobalRatio ? validScreeningNum : lowVisionNum).floatValue());
        setLightMyopiaNum(lightMyopiaNum);
        setLightMyopiaRatio(MathUtil.divide(lightMyopiaNum, isGlobalRatio ? validScreeningNum : lowVisionNum).floatValue());
        setMiddleMyopiaNum(middleMyopiaNum);
        setMiddleMyopiaRatio(MathUtil.divide(middleMyopiaNum, isGlobalRatio ? validScreeningNum : lowVisionNum).floatValue());
        setHighMyopiaNum(highMyopiaNum);
        setHighMyopiaRatio(MathUtil.divide(highMyopiaNum, isGlobalRatio ? validScreeningNum : lowVisionNum).floatValue());
    }

}
