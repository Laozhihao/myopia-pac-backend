package com.wupol.myopia.business.api.management.domain.vo.report;

import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.service.report.EntityFunction;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * 龋齿监测统计
 *
 * @author hang.yuan
 * @date 2022/6/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SaprodontiaNum extends EntityFunction implements Num {

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
    private String dmftRatio;

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

    // ========= 不带% =============

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

    // ========= 带% =============

    /**
     * 龋患率
     */
    private String saprodontiaRatioStr;
    /**
     * 龋失率
     */
    private String saprodontiaLossRatioStr;
    /**
     * 龋补率
     */
    private String saprodontiaRepairRatioStr;
    /**
     * 龋患（失、补）率
     */
    private String saprodontiaLossAndRepairRatioStr;

    /**
     * 龋患（失、补）构成比
     */
    private String saprodontiaLossAndRepairTeethRatioStr;

    /**
     * 性别
     */
    private Integer gender;

    public SaprodontiaNum setGender(Integer gender) {
        this.gender = gender;
        return this;
    }

    public SaprodontiaNum build(List<StatConclusion> statConclusionList) {
        if (Objects.isNull(statConclusionList)) {
            this.validScreeningNum = ReportConst.ZERO;
            this.dmftNum = ReportConst.ZERO;
            this.saprodontiaLossAndRepairTeethNum = ReportConst.ZERO;
            this.saprodontiaNum = ReportConst.ZERO;
            this.saprodontiaLossNum = ReportConst.ZERO;
            this.saprodontiaRepairNum = ReportConst.ZERO;
            this.saprodontiaLossAndRepairNum = ReportConst.ZERO;
            return this;
        }
        this.validScreeningNum = statConclusionList.size();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(ReportConst.ZERO);
        this.dmftNum = statConclusionList.stream().filter(Objects::nonNull).filter(predicateTrue).mapToInt(totalFunction).sum();

        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(ReportConst.ZERO);
        this.saprodontiaLossAndRepairTeethNum = statConclusionList.stream().filter(Objects::nonNull).filter(lossAndRepairPredicateTrue).mapToInt(lossAndRepairTotalFunction).sum();

        this.saprodontiaNum = getCount(statConclusionList, StatConclusion::getIsSaprodontia);
        this.saprodontiaLossNum = getCount(statConclusionList, StatConclusion::getIsSaprodontiaLoss);
        this.saprodontiaRepairNum = getCount(statConclusionList, StatConclusion::getIsSaprodontiaRepair);
        this.saprodontiaLossAndRepairNum = (int) statConclusionList.stream()
                .filter(lossAndRepairPredicateTrue).count();

        return this;
    }

    /**
     * 不带%
     */
    public SaprodontiaNum ratioNotSymbol() {
        this.dmftRatio = Optional.ofNullable(MathUtil.num(dmftNum, getTotal())).orElse(ReportConst.ZERO_STR);
        this.saprodontiaRatio = getRatioNotSymbol(saprodontiaNum, getTotal());
        this.saprodontiaLossRatio = getRatioNotSymbol(saprodontiaLossNum, getTotal());
        this.saprodontiaRepairRatio = getRatioNotSymbol(saprodontiaRepairNum, getTotal());
        this.saprodontiaLossAndRepairRatio = getRatioNotSymbol(saprodontiaLossAndRepairNum, getTotal());
        this.saprodontiaLossAndRepairTeethRatio = getRatioNotSymbol(saprodontiaLossAndRepairTeethNum, dmftNum);
        return this;
    }

    /**
     * 带%
     */
    public SaprodontiaNum ratio() {
        this.dmftRatio = Optional.ofNullable(MathUtil.num(dmftNum, getTotal())).orElse(ReportConst.ZERO_STR);
        this.saprodontiaRatioStr = getRatio(saprodontiaNum, getTotal());
        this.saprodontiaLossRatioStr = getRatio(saprodontiaLossNum, getTotal());
        this.saprodontiaRepairRatioStr = getRatio(saprodontiaRepairNum, getTotal());
        this.saprodontiaLossAndRepairRatioStr = getRatio(saprodontiaLossAndRepairNum, getTotal());
        this.saprodontiaLossAndRepairTeethRatioStr = getRatio(saprodontiaLossAndRepairTeethNum, dmftNum);
        return this;
    }

    private static Integer getTotal(){
        return MAP.get(0);
    }

    public static Map<Integer,Integer> MAP = Maps.newConcurrentMap();

    public SaprodontiaMonitorTable buildTable(){
        SaprodontiaMonitorTable saprodontiaMonitorTable= new SaprodontiaMonitorTable();
        saprodontiaMonitorTable.setValidScreeningNum(validScreeningNum);
        saprodontiaMonitorTable.setDmftRatio(dmftRatio);
        saprodontiaMonitorTable.setSaprodontiaNum(saprodontiaNum);
        saprodontiaMonitorTable.setSaprodontiaRatio(saprodontiaRatio);
        saprodontiaMonitorTable.setSaprodontiaLossNum(saprodontiaLossNum);
        saprodontiaMonitorTable.setSaprodontiaLossRatio(saprodontiaLossRatio);
        saprodontiaMonitorTable.setSaprodontiaRepairNum(saprodontiaRepairNum);
        saprodontiaMonitorTable.setSaprodontiaRepairRatio(saprodontiaRepairRatio);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairRatio(saprodontiaLossAndRepairRatio);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethNum(saprodontiaLossAndRepairTeethNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethRatio(saprodontiaLossAndRepairTeethRatio);
        return saprodontiaMonitorTable;
    }

    public SaprodontiaMonitorVariableVO buildSaprodontiaMonitorVariableVO(){
        SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = new SaprodontiaMonitorVariableVO();
        saprodontiaMonitorVariableVO.setDmftRatio(dmftRatio);
        saprodontiaMonitorVariableVO.setSaprodontiaRatio(saprodontiaRatioStr);
        saprodontiaMonitorVariableVO.setSaprodontiaRepairRatio(saprodontiaRepairRatioStr);
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairRatio(saprodontiaLossAndRepairRatioStr);
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairTeethRatio(saprodontiaLossAndRepairTeethRatioStr);
        return saprodontiaMonitorVariableVO;
    }

    public SaprodontiaSchoolAge buildSaprodontiaSchoolAge(){
        SaprodontiaSchoolAge saprodontiaSchoolAge = new SaprodontiaSchoolAge();
        saprodontiaSchoolAge.setSaprodontiaRatio(saprodontiaRatioStr);
        saprodontiaSchoolAge.setSaprodontiaLossRatio(saprodontiaLossRatioStr);
        saprodontiaSchoolAge.setSaprodontiaRepairRatio(saprodontiaRepairRatioStr);
        return saprodontiaSchoolAge;
    }
}