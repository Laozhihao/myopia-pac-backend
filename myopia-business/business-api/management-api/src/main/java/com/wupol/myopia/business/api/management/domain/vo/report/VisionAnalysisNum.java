package com.wupol.myopia.business.api.management.domain.vo.report;

import cn.hutool.core.collection.CollectionUtil;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.service.report.EntityFunction;
import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.VisionCorrection;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 视力分析统计
 * @author hang.yuan
 * @date 2022/6/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VisionAnalysisNum extends EntityFunction {
    /**
     * 有效筛查人数
     */
    private Integer validScreeningNum;

    /**
     * 视力低下人数
     */
    private Integer lowVisionNum;
    /**
     * 视力低下率
     */
    private BigDecimal lowVisionRatio;

    /**
     * 近视人数
     */
    private Integer myopiaNum;
    /**
     * 近视率
     */
    private BigDecimal myopiaRatio;

    /**
     * 夜戴角膜塑形镜人数
     */
    private Integer nightWearingOrthokeratologyLensesNum;

    /**
     * 夜戴角膜塑形镜率
     */
    private BigDecimal nightWearingOrthokeratologyLensesRatio;

    /**
     * 近视前期人数
     */
    private Integer myopiaLevelEarlyNum;

    /**
     * 近视前期率
     */
    private BigDecimal myopiaLevelEarlyRatio;

    /**
     * 低度近视人数
     */
    private Integer lowMyopiaNum;

    /**
     * 低度近视率
     */
    private BigDecimal lowMyopiaRatio;

    /**
     * 高度近视人数
     */
    private Integer highMyopiaNum;
    /**
     * 高度近视率
     */
    private BigDecimal highMyopiaRatio;

    /**
     * 散光人数
     */
    private Integer astigmatismNum;

    /**
     * 散光率
     */
    private BigDecimal astigmatismRatio;

    /**
     * 近视足矫人数
     */
    private Integer myopiaEnoughCorrectedNum;

    /**
     * 近视足矫率
     */
    private BigDecimal myopiaEnoughCorrectedRatio;

    /**
     * 近视未矫人数
     */
    private Integer myopiaUncorrectedNum;

    /**
     * 近视未矫率
     */
    private BigDecimal myopiaUncorrectedRatio;

    /**
     * 近视欠矫人数
     */
    private Integer myopiaUnderCorrectedNum;

    /**
     * 近视欠矫率
     */
    private BigDecimal myopiaUnderCorrectedRatio;

    public VisionAnalysisNum build(List<StatConclusion> statConclusionList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            this.validScreeningNum=ReportConst.ZERO;
            this.lowVisionNum=ReportConst.ZERO;
            this.myopiaNum=ReportConst.ZERO;
            this.nightWearingOrthokeratologyLensesNum=ReportConst.ZERO;
            this.myopiaLevelEarlyNum=ReportConst.ZERO;
            this.lowMyopiaNum=ReportConst.ZERO;
            this.highMyopiaNum=ReportConst.ZERO;
            this.astigmatismNum=ReportConst.ZERO;
            this.myopiaEnoughCorrectedNum=ReportConst.ZERO;
            this.myopiaUncorrectedNum=ReportConst.ZERO;
            this.myopiaUnderCorrectedNum=ReportConst.ZERO;
            return this;
        }
        this.validScreeningNum = statConclusionList.size();
        this.lowVisionNum = getCount(statConclusionList, StatConclusion::getIsLowVision);
        this.myopiaNum = getCount(statConclusionList, StatConclusion::getIsMyopia);
        this.nightWearingOrthokeratologyLensesNum = getCount(statConclusionList, StatConclusion::getGlassesType, GlassesTypeEnum.ORTHOKERATOLOGY.code);
        this.myopiaLevelEarlyNum = getCount(statConclusionList, StatConclusion::getMyopiaLevel, MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code);
        this.lowMyopiaNum = getCount(statConclusionList, StatConclusion::getMyopiaLevel, MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code);
        this.highMyopiaNum = getCount(statConclusionList, StatConclusion::getMyopiaLevel, MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code);
        this.astigmatismNum = getCount(statConclusionList, StatConclusion::getIsAstigmatism);
        this.myopiaEnoughCorrectedNum = getCount(statConclusionList, StatConclusion::getVisionCorrection, VisionCorrection.ENOUGH_CORRECTED.code);
        this.myopiaUncorrectedNum = getCount(statConclusionList, StatConclusion::getVisionCorrection, VisionCorrection.UNCORRECTED.code);
        this.myopiaUnderCorrectedNum = getCount(statConclusionList, StatConclusion::getVisionCorrection, VisionCorrection.UNDER_CORRECTED.code);
        return this;
    }

    /**
     * 不带%
     */
    public VisionAnalysisNum ratioNotSymbol() {

        this.lowVisionRatio = getRatioNotSymbol(lowVisionNum, validScreeningNum);
        this.myopiaRatio = getRatioNotSymbol(myopiaNum, validScreeningNum);
        this.nightWearingOrthokeratologyLensesRatio = getRatioNotSymbol(nightWearingOrthokeratologyLensesNum, validScreeningNum);
        this.myopiaLevelEarlyRatio = getRatioNotSymbol(myopiaLevelEarlyNum, validScreeningNum);
        this.lowMyopiaRatio = getRatioNotSymbol(lowMyopiaNum, validScreeningNum);
        this.highMyopiaRatio = getRatioNotSymbol(highMyopiaNum, validScreeningNum);
        this.astigmatismRatio = getRatioNotSymbol(astigmatismNum, validScreeningNum);
        this.myopiaEnoughCorrectedRatio = getRatioNotSymbol(myopiaEnoughCorrectedNum, validScreeningNum);
        this.myopiaUncorrectedRatio = getRatioNotSymbol(myopiaUncorrectedNum, validScreeningNum);
        this.myopiaUnderCorrectedRatio = getRatioNotSymbol(myopiaUnderCorrectedNum, validScreeningNum);

        return this;
    }

    public SchoolCommonDiseaseReportVO.VisionAnalysisVO buildVisionAnalysisVO(){
        SchoolCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO = new SchoolCommonDiseaseReportVO.VisionAnalysisVO();
        visionAnalysisVO.setValidScreeningNum(validScreeningNum);
        visionAnalysisVO.setLowVision(getItem(VisionAnalysisNum::getLowVisionNum, VisionAnalysisNum::getLowVisionRatio));
        visionAnalysisVO.setMyopia(getItem(VisionAnalysisNum::getMyopiaNum, VisionAnalysisNum::getMyopiaRatio));
        visionAnalysisVO.setNightWearingOrthokeratologyLenses(getItem(VisionAnalysisNum::getNightWearingOrthokeratologyLensesNum, VisionAnalysisNum::getNightWearingOrthokeratologyLensesRatio));
        visionAnalysisVO.setMyopiaLevelEarly(getItem(VisionAnalysisNum::getMyopiaLevelEarlyNum, VisionAnalysisNum::getMyopiaLevelEarlyRatio));
        visionAnalysisVO.setLowMyopia(getItem(VisionAnalysisNum::getLowMyopiaNum, VisionAnalysisNum::getLowMyopiaRatio));
        visionAnalysisVO.setHighMyopia(getItem(VisionAnalysisNum::getHighMyopiaNum, VisionAnalysisNum::getHighMyopiaRatio));
        visionAnalysisVO.setAstigmatism(getItem(VisionAnalysisNum::getAstigmatismNum, VisionAnalysisNum::getAstigmatismRatio));
        visionAnalysisVO.setMyopiaEnoughCorrected(getItem(VisionAnalysisNum::getMyopiaEnoughCorrectedNum, VisionAnalysisNum::getMyopiaEnoughCorrectedRatio));
        visionAnalysisVO.setMyopiaUncorrected(getItem(VisionAnalysisNum::getMyopiaUncorrectedNum, VisionAnalysisNum::getMyopiaUncorrectedRatio));
        visionAnalysisVO.setMyopiaUnderCorrected(getItem(VisionAnalysisNum::getMyopiaUnderCorrectedNum, VisionAnalysisNum::getMyopiaUnderCorrectedRatio));
        return visionAnalysisVO;
    }


    private SchoolCommonDiseaseReportVO.Item getItem(Function<VisionAnalysisNum,Integer> function, Function<VisionAnalysisNum,BigDecimal> mapper){
        Integer num = Optional.of(this).map(function).orElse(ReportConst.ZERO);
        BigDecimal ratio = Optional.of(this).map(mapper).orElse(ReportConst.ZERO_BIG_DECIMAL);
        if (ObjectsUtil.allNotNull(num,ratio)){
            return new SchoolCommonDiseaseReportVO.Item(num,ratio);
        }
        return null;
    }
}