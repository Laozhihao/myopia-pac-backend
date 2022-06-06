package com.wupol.myopia.business.api.management.domain.vo.report;

import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.service.report.EntityFunction;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 龋齿监测统计
 * @author hang.yuan
 * @date 2022/6/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningNum extends EntityFunction {
    /**
     * 筛查人数
     */
    private Integer validScreeningNum;

    /**
     * 有龋人数
     */
    private Integer saprodontiaNum;

    /**
     * 龋患（失、补）人数
     */
    private Integer saprodontiaLossAndRepairNum;

    /**
     * 超重数
     */
    private Integer overweightNum;
    /**
     * 肥胖数
     */
    private Integer obeseNum;
    /**
     * 营养不良数
     */
    private Integer malnourishedNum;
    /**
     * 生长迟缓数据
     */
    private Integer stuntingNum;

    /**
     * 血压偏高人数
     */
    private Integer highBloodPressureNum;

    /**
     * 脊柱弯曲异常人数
     */
    private Integer abnormalSpineCurvatureNum;


    //=========== 不带% =============
    /**
     * 龋患率
     */
    private BigDecimal saprodontiaRatio;

    /**
     * 龋患（失、补）率
     */
    private BigDecimal saprodontiaLossAndRepairRatio;
    /**
     * 超重率
     */
    private BigDecimal overweightRatio;
    /**
     * 肥胖率
     */
    private BigDecimal obeseRatio;

    /**
     * 营养不良率
     */
    private BigDecimal malnourishedRatio;

    /**
     * 生长迟缓率
     */
    private BigDecimal stuntingRatio;

    /**
     * 血压偏高率
     */
    private BigDecimal highBloodPressureRatio;

    /**
     * 脊柱弯曲异常率
     */
    private BigDecimal abnormalSpineCurvatureRatio;

    public ScreeningNum build(List<StatConclusion> statConclusionList) {
        this.validScreeningNum = statConclusionList.size();
        this.saprodontiaNum = getCount(statConclusionList, StatConclusion::getIsSaprodontia);
        this.saprodontiaLossAndRepairNum = (int) statConclusionList.stream()
                .filter(sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair())).count();
        this.overweightNum = getCount(statConclusionList, StatConclusion::getIsOverweight);
        this.obeseNum = getCount(statConclusionList, StatConclusion::getIsObesity);
        this.malnourishedNum = getCount(statConclusionList, StatConclusion::getIsMalnutrition);
        this.stuntingNum = getCount(statConclusionList, StatConclusion::getIsStunting);
        this.abnormalSpineCurvatureNum = getCount(statConclusionList, StatConclusion::getIsSpinalCurvature);
        this.highBloodPressureNum = (int) statConclusionList.stream()
                .filter(sc -> Objects.equals(Boolean.FALSE, sc.getIsNormalBloodPressure())).count();

        return this;
    }

    /**
     * 不带%
     */
    public ScreeningNum ratioNotSymbol() {
        this.saprodontiaRatio = getRatioNotSymbol(saprodontiaNum, getTotal());
        this.saprodontiaLossAndRepairRatio = getRatioNotSymbol(saprodontiaLossAndRepairNum, getTotal());
        this.overweightRatio = getRatioNotSymbol(overweightNum, getTotal());
        this.obeseRatio = getRatioNotSymbol(obeseNum, getTotal());
        this.stuntingRatio = getRatioNotSymbol(stuntingNum, getTotal());
        this.malnourishedRatio = getRatioNotSymbol(malnourishedNum, getTotal());
        this.abnormalSpineCurvatureRatio = getRatioNotSymbol(abnormalSpineCurvatureNum, getTotal());
        this.highBloodPressureRatio = getRatioNotSymbol(highBloodPressureNum, getTotal());
        return this;
    }

    private static Integer getTotal(){
        return MAP.get(0);
    }

    public static Map<Integer,Integer> MAP = Maps.newConcurrentMap();

    public ScreeningMonitorTable buildTable(){
        ScreeningMonitorTable schoolScreeningMonitorTable = new ScreeningMonitorTable();
        schoolScreeningMonitorTable.setValidScreeningNum(validScreeningNum);
        schoolScreeningMonitorTable.setSaprodontiaNum(saprodontiaNum);
        schoolScreeningMonitorTable.setSaprodontiaRatio(saprodontiaRatio);
        schoolScreeningMonitorTable.setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum);
        schoolScreeningMonitorTable.setSaprodontiaLossAndRepairRatio(saprodontiaLossAndRepairRatio);
        schoolScreeningMonitorTable.setOverweightNum(overweightNum);
        schoolScreeningMonitorTable.setOverweightRatio(overweightRatio);
        schoolScreeningMonitorTable.setObeseNum(obeseNum);
        schoolScreeningMonitorTable.setObeseRatio(obeseRatio);
        schoolScreeningMonitorTable.setStuntingNum(stuntingNum);
        schoolScreeningMonitorTable.setStuntingRatio(stuntingRatio);
        schoolScreeningMonitorTable.setMalnourishedNum(malnourishedNum);
        schoolScreeningMonitorTable.setMalnourishedRatio(malnourishedRatio);
        schoolScreeningMonitorTable.setAbnormalSpineCurvatureNum(abnormalSpineCurvatureNum);
        schoolScreeningMonitorTable.setAbnormalSpineCurvatureRatio(abnormalSpineCurvatureRatio);
        schoolScreeningMonitorTable.setHighBloodPressureNum(highBloodPressureNum);
        schoolScreeningMonitorTable.setHighBloodPressureRatio(highBloodPressureRatio);
        return schoolScreeningMonitorTable;
    }
}
