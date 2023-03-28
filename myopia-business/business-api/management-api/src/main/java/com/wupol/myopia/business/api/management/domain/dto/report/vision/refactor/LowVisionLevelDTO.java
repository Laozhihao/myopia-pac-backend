package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import com.wupol.myopia.business.common.utils.constant.NumberCommonConst;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

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
public class LowVisionLevelDTO {

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
     * 低度视力不良人数
     */
    private Integer lightLowVisionNum;

    /**
     * 低度视力不良率
     */
    private Float lightLowVisionRatio;

    /**
     * 中度视力不良人数
     */
    private Integer middleLowVisionNum;

    /**
     * 中度视力不良率
     */
    private Float middleLowVisionRatio;

    /**
     * 高度视力不良人数
     */
    private Integer highLowVisionNum;

    /**
     * 高度视力不良率
     */
    private Float highLowVisionRatio;

    /**
     * 夜戴
     */
    private Integer nightWearingNum;

    /**
     * 夜戴率
     */
    private Float nightWearingRatio;

    public void empty() {
        setValidScreeningNum(0);
        setLowVisionNum(0);
        setLowVisionRatio(0.0f);
        setLightLowVisionNum(0);
        setLightLowVisionRatio(0.0f);
        setMiddleLowVisionNum(0);
        setMiddleLowVisionRatio(0.0f);
        setHighLowVisionNum(0);
        setHighLowVisionRatio(0.0f);
        setNightWearingNum(0);
        setNightWearingRatio(0.0f);
    }

    /**
     * 生成视力情况(全局比例)
     * @param validScreeningNum
     * @param lowVisionNum
     * @param lightMyopiaNum
     * @param middleMyopiaNum
     * @param highMyopiaNum
     * @param nightWearingNum
     */
    public void generateData(int validScreeningNum, int lowVisionNum, int lightMyopiaNum, int middleMyopiaNum, int highMyopiaNum, int nightWearingNum) {
        generateData(validScreeningNum, lowVisionNum, lightMyopiaNum, middleMyopiaNum, highMyopiaNum, nightWearingNum, true);
    }

    /**
     * 生成视力情况
     * @param validScreeningNum
     * @param lowVisionNum
     * @param lightMyopiaNum
     * @param middleMyopiaNum
     * @param highMyopiaNum
     * @param nightWearingNum
     * @param isGlobalRatio 是否全局占比，若为全局占比，不良率分母为有效筛查人数，否则为视力不良人数
     */
    public void generateData(int validScreeningNum, int lowVisionNum, int lightMyopiaNum, int middleMyopiaNum, int highMyopiaNum, int nightWearingNum, boolean isGlobalRatio) {
        setValidScreeningNum(validScreeningNum);
        setLowVisionNum(lowVisionNum);
        setLowVisionRatio(MathUtil.divideFloat(lowVisionNum, isGlobalRatio ? validScreeningNum : lowVisionNum));
        setLightLowVisionNum(lightMyopiaNum);
        setLightLowVisionRatio(MathUtil.divideFloat(lightMyopiaNum, isGlobalRatio ? validScreeningNum : lowVisionNum));
        setMiddleLowVisionNum(middleMyopiaNum);
        setMiddleLowVisionRatio(MathUtil.divideFloat(middleMyopiaNum, isGlobalRatio ? validScreeningNum : lowVisionNum));
        setHighLowVisionNum(highMyopiaNum);
        setHighLowVisionRatio(MathUtil.divideFloat(highMyopiaNum, isGlobalRatio ? validScreeningNum : lowVisionNum));
        setNightWearingNum(nightWearingNum);
        if (isGlobalRatio) {
            setNightWearingRatio(MathUtil.divideFloat(nightWearingNum, validScreeningNum));
        } else {
            // 若是计算占总视力不良数占比，用100-轻度占比-中度占比-高度占比，得到夜戴的%比，避免四者加起来不为1
            setNightWearingRatio(new BigDecimal(100)
                    .subtract(new BigDecimal(getLightLowVisionRatio()))
                    .subtract(new BigDecimal(getMiddleLowVisionRatio()))
                    .subtract(new BigDecimal(getHighLowVisionRatio()))
                    .setScale(NumberCommonConst.TWO_INT, BigDecimal.ROUND_HALF_UP).floatValue());
        }
    }

}
