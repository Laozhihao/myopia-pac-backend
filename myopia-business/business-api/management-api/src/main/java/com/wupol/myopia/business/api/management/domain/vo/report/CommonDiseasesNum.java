package com.wupol.myopia.business.api.management.domain.vo.report;

import cn.hutool.core.collection.CollectionUtil;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.service.report.EntityFunction;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * 常见病统计
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommonDiseasesNum extends EntityFunction {
    /**
     * 筛查人数
     */
    private Integer validScreeningNum;
    /**
     * 龋失补牙数
     */
    private Integer dmftNum;

    /**
     * 龋均
     */
    private BigDecimal dmftRatio;

    /**
     * 有龋人数
     */
    private Integer saprodontiaNum;

    /**
     * 龋失人数
     */
    private Integer saprodontiaLossNum;

    /**
     * 龋补人数
     */
    private Integer saprodontiaRepairNum;

    /**
     * 龋患（失、补）人数
     */
    private Integer saprodontiaLossAndRepairNum;

    /**
     * 龋患（失、补）牙数
     */
    private Integer saprodontiaLossAndRepairTeethNum;

    /**
     * 超重人数
     */
    private Integer overweightNum;

    /**
     * 肥胖人数
     */
    private Integer obeseNum;

    /**
     * 血压偏高人数
     */
    private Integer highBloodPressureNum;

    /**
     * 脊柱弯曲异常人数
     */
    private Integer abnormalSpineCurvatureNum;

    // ============ 不带% ==============
    /**
     * 龋患率
     */
    private BigDecimal saprodontiaRatio;
    /**
     * 龋失率
     */
    private BigDecimal saprodontiaLossRatio;

    /**
     * 龋补率
     */
    private BigDecimal saprodontiaRepairRatio;
    /**
     * 龋患（失、补）率
     */
    private BigDecimal saprodontiaLossAndRepairRatio;

    /**
     * 龋患（失、补）构成比
     */
    private BigDecimal saprodontiaLossAndRepairTeethRatio;

    /**
     * 超重率
     */
    private BigDecimal overweightRatio;
    /**
     * 肥胖率
     */
    private BigDecimal obeseRatio;

    /**
     * 血压偏高率
     */
    private BigDecimal highBloodPressureRatio;

    /**
     * 脊柱弯曲异常率
     */
    private BigDecimal abnormalSpineCurvatureRatio;

    public CommonDiseasesNum build(List<StatConclusion> statConclusionList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            this.validScreeningNum =ReportConst.ZERO;
            this.dmftNum =ReportConst.ZERO;
            this.saprodontiaLossAndRepairTeethNum =ReportConst.ZERO;
            this.saprodontiaNum =ReportConst.ZERO;
            this.saprodontiaLossNum =ReportConst.ZERO;
            this.saprodontiaRepairNum =ReportConst.ZERO;
            this.saprodontiaLossAndRepairNum =ReportConst.ZERO;
            this.overweightNum =ReportConst.ZERO;
            this.obeseNum =ReportConst.ZERO;
            this.abnormalSpineCurvatureNum =ReportConst.ZERO;
            this.highBloodPressureNum =ReportConst.ZERO;
            return this;
        }

        this.validScreeningNum = statConclusionList.size();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(ReportConst.ZERO);
        this.dmftNum = statConclusionList.stream()
                .filter(Objects::nonNull)
                .filter(predicateTrue).mapToInt(totalFunction).sum();

        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(ReportConst.ZERO);
        this.saprodontiaLossAndRepairTeethNum = statConclusionList.stream()
                .filter(Objects::nonNull)
                .filter(lossAndRepairPredicateTrue).mapToInt(lossAndRepairTotalFunction).sum();
        this.saprodontiaNum = getCount(statConclusionList, StatConclusion::getIsSaprodontia);
        this.saprodontiaLossNum = getCount(statConclusionList, StatConclusion::getIsSaprodontiaLoss);
        this.saprodontiaRepairNum = getCount(statConclusionList, StatConclusion::getIsSaprodontiaRepair);
        this.saprodontiaLossAndRepairNum = (int) statConclusionList.stream().filter(lossAndRepairPredicateTrue).count();
        this.overweightNum = getCount(statConclusionList, StatConclusion::getIsOverweight);
        this.obeseNum = getCount(statConclusionList, StatConclusion::getIsObesity);
        this.abnormalSpineCurvatureNum = getCount(statConclusionList, StatConclusion::getIsSpinalCurvature);
        this.highBloodPressureNum = (int) statConclusionList.stream().filter(sc -> Objects.equals(Boolean.FALSE, sc.getIsNormalBloodPressure())).count();
        return this;
    }

    /**
     * 不带%
     */
    public CommonDiseasesNum ratioNotSymbol() {
        this.dmftRatio = Optional.ofNullable(MathUtil.numNotSymbol(dmftNum, validScreeningNum)).orElse(ReportConst.ZERO_BIG_DECIMAL);
        this.saprodontiaRatio = getRatioNotSymbol(saprodontiaNum, validScreeningNum);
        this.saprodontiaLossRatio = getRatioNotSymbol(saprodontiaLossNum, validScreeningNum);
        this.saprodontiaRepairRatio = getRatioNotSymbol(saprodontiaRepairNum, validScreeningNum);
        this.saprodontiaLossAndRepairRatio = getRatioNotSymbol(saprodontiaLossAndRepairNum, validScreeningNum);
        this.saprodontiaLossAndRepairTeethRatio = getRatioNotSymbol(saprodontiaLossAndRepairTeethNum, dmftNum);
        this.overweightRatio = getRatioNotSymbol(overweightNum, validScreeningNum);
        this.obeseRatio = getRatioNotSymbol(obeseNum, validScreeningNum);
        this.abnormalSpineCurvatureRatio = getRatioNotSymbol(abnormalSpineCurvatureNum, validScreeningNum);
        this.highBloodPressureRatio = getRatioNotSymbol(highBloodPressureNum, validScreeningNum);
        return this;
    }

    private CommonDiseasesItemVO getItem(Function<CommonDiseasesNum,Integer> function, Function<CommonDiseasesNum,BigDecimal> mapper){
        Integer num = Optional.of(this).map(function).orElse(ReportConst.ZERO);
        BigDecimal ratio = Optional.of(this).map(mapper).orElse(ReportConst.ZERO_BIG_DECIMAL);
        if (ObjectsUtil.allNotNull(num,ratio)){
            return new CommonDiseasesItemVO(num,ratio);
        }
        return new CommonDiseasesItemVO(ReportConst.ZERO,ReportConst.ZERO_BIG_DECIMAL);
    }

    public CommonDiseasesAnalysisVariableVO buidCommonDiseasesAnalysisVariableVO(){
        CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO = new CommonDiseasesAnalysisVariableVO();
        commonDiseasesAnalysisVariableVO.setValidScreeningNum(validScreeningNum);
        commonDiseasesAnalysisVariableVO.setAbnormalSpineCurvatureNum(abnormalSpineCurvatureNum);
        commonDiseasesAnalysisVariableVO.setDmft(getItem(CommonDiseasesNum::getDmftNum,CommonDiseasesNum::getDmftRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontia(getItem(CommonDiseasesNum::getSaprodontiaNum,CommonDiseasesNum::getSaprodontiaRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaLoss(getItem(CommonDiseasesNum::getSaprodontiaLossNum,CommonDiseasesNum::getSaprodontiaLossRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaRepair(getItem(CommonDiseasesNum::getSaprodontiaRepairNum,CommonDiseasesNum::getSaprodontiaRepairRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaLossAndRepair(getItem(CommonDiseasesNum::getSaprodontiaLossAndRepairNum,CommonDiseasesNum::getSaprodontiaLossAndRepairRatio));
        commonDiseasesAnalysisVariableVO.setSaprodontiaLossAndRepairTeeth(getItem(CommonDiseasesNum::getSaprodontiaLossAndRepairTeethNum,CommonDiseasesNum::getSaprodontiaLossAndRepairTeethRatio));
        commonDiseasesAnalysisVariableVO.setOverweight(getItem(CommonDiseasesNum::getOverweightNum,CommonDiseasesNum::getOverweightRatio));
        commonDiseasesAnalysisVariableVO.setObese(getItem(CommonDiseasesNum::getObeseNum,CommonDiseasesNum::getObeseRatio));
        commonDiseasesAnalysisVariableVO.setHighBloodPressure(getItem(CommonDiseasesNum::getHighBloodPressureNum,CommonDiseasesNum::getHighBloodPressureRatio));
        commonDiseasesAnalysisVariableVO.setAbnormalSpineCurvature(getItem(CommonDiseasesNum::getAbnormalSpineCurvatureNum,CommonDiseasesNum::getAbnormalSpineCurvatureRatio));
        return commonDiseasesAnalysisVariableVO;
    }

}