package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import com.wupol.myopia.business.common.utils.util.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 近视情况（性别）
 * @Author wulizhou
 * @Date 2022/12/26 15:42
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GenderMyopiaInfoDTO {

    /**
     * 有效筛查人数
     */
    private int validScreeningNum;

    /**
     * 近视人数
     */
    private int myopiaNum;

    /**
     * 近视率
     */
    private Float myopiaRatio;

    /**
     * 有效筛查人数（男）
     */
    private int maleNum;

    /**
     * 近视人数（男）
     */
    private int maleMyopiaNum;

    /**
     * 近视率（男）
     */
    private Float maleMyopiaRatio;

    /**
     * 有效筛查人数（女）
     */
    private int femaleNum;

    /**
     * 近视人数（女）
     */
    private int femaleMyopiaNum;

    /**
     * 近视率（女）
     */
    private Float femaleMyopiaRatio;

    public void empty() {
        setValidScreeningNum(0);
        setMyopiaNum(0);
        setMyopiaRatio(0.0f);
        setMaleNum(0);
        setMaleMyopiaNum(0);
        setMaleMyopiaRatio(0.0f);
        setFemaleNum(0);
        setFemaleMyopiaNum(0);
        setFemaleMyopiaRatio(0.0f);
    }

    /**
     * 生成近视情况
     * @param validScreeningNum
     * @param myopiaNum
     * @param maleNum
     * @param maleMyopiaNum
     * @param femaleNum
     * @param femaleMyopiaNum
     */
    public void generateData(int validScreeningNum, int myopiaNum, int maleNum, int maleMyopiaNum, int femaleNum, int femaleMyopiaNum) {
        setValidScreeningNum(validScreeningNum);
        setMyopiaNum(myopiaNum);
        setMyopiaRatio(MathUtil.divideFloat(myopiaNum, validScreeningNum));
        setMaleNum(maleNum);
        setMaleMyopiaNum(maleMyopiaNum);
        setMaleMyopiaRatio(MathUtil.divideFloat(maleNum, maleMyopiaNum));
        setFemaleNum(femaleNum);
        setFemaleMyopiaNum(femaleMyopiaNum);
        setFemaleMyopiaRatio(MathUtil.divideFloat(femaleNum, femaleMyopiaNum));
    }

}
