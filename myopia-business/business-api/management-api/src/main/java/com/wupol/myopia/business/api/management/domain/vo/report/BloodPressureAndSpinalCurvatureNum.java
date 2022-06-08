package com.wupol.myopia.business.api.management.domain.vo.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.service.report.EntityFunction;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 血压与脊柱弯曲异常监测统计实体
 * @author hang.yuan
 * @date 2022/6/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BloodPressureAndSpinalCurvatureNum extends EntityFunction implements Num {

    /**
     * 筛查人数
     */
    private Integer validScreeningNum;
    /**
     * 血压偏高人数
     */
    private Integer highBloodPressureNum;

    /**
     * 脊柱弯曲异常人数
     */
    private Integer abnormalSpineCurvatureNum;


    //============= 不带% ============
    /**
     * 血压偏高率
     */
    private BigDecimal highBloodPressureRatio;

    /**
     * 脊柱弯曲异常率
     */
    private BigDecimal abnormalSpineCurvatureRatio;


    //============= 带% ============
    /**
     * 血压偏高率
     */
    private String highBloodPressureRatioStr;

    /**
     * 脊柱弯曲异常率
     */
    private String abnormalSpineCurvatureRatioStr;

    /**
     * 性别
     */
    private Integer gender;

    public BloodPressureAndSpinalCurvatureNum build(List<StatConclusion> statConclusionList) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            this.validScreeningNum = ReportConst.ZERO;
            this.abnormalSpineCurvatureNum = ReportConst.ZERO;
            this.highBloodPressureNum = ReportConst.ZERO;
            return this;
        }
        this.validScreeningNum = statConclusionList.size();

        this.abnormalSpineCurvatureNum = getCount(statConclusionList, StatConclusion::getIsSpinalCurvature);
        this.highBloodPressureNum = (int) statConclusionList.stream()
                .filter(sc -> Objects.equals(Boolean.FALSE, sc.getIsNormalBloodPressure())).count();
        return this;
    }

    /**
     * 不带%
     */
    public BloodPressureAndSpinalCurvatureNum ratioNotSymbol() {
        this.abnormalSpineCurvatureRatio = getRatioNotSymbol(abnormalSpineCurvatureNum, getTotal());
        this.highBloodPressureRatio = getRatioNotSymbol(highBloodPressureNum, getTotal());
        return this;
    }

    /**
     * 带%
     */
    public BloodPressureAndSpinalCurvatureNum ratio() {
        this.abnormalSpineCurvatureRatioStr = getRatio(abnormalSpineCurvatureNum, getTotal());
        this.highBloodPressureRatioStr = getRatio(highBloodPressureNum, getTotal());
        return this;
    }

    public BloodPressureAndSpinalCurvatureNum setGender(Integer gender) {
        this.gender = gender;
        return this;
    }

    private static Integer getTotal(){
        return MAP.get(0);
    }

    public static Map<Integer,Integer> MAP = Maps.newConcurrentMap();


    public BloodPressureAndSpinalCurvatureMonitorVariableVO buildBloodPressureAndSpinalCurvatureMonitorVariableVO(){
        BloodPressureAndSpinalCurvatureMonitorVariableVO variableVO = new BloodPressureAndSpinalCurvatureMonitorVariableVO();
        variableVO.setAbnormalSpineCurvatureRatio(abnormalSpineCurvatureRatioStr);
        variableVO.setHighBloodPressureRatio(highBloodPressureRatioStr);
        return variableVO;
    }

    public BloodPressureAndSpinalCurvatureMonitorTable buildTable(){
        BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable= new BloodPressureAndSpinalCurvatureMonitorTable();
        bloodPressureAndSpinalCurvatureMonitorTable.setValidScreeningNum(validScreeningNum);
        bloodPressureAndSpinalCurvatureMonitorTable.setAbnormalSpineCurvatureNum(abnormalSpineCurvatureNum);
        bloodPressureAndSpinalCurvatureMonitorTable.setAbnormalSpineCurvatureRatio(abnormalSpineCurvatureRatio);
        bloodPressureAndSpinalCurvatureMonitorTable.setHighBloodPressureNum(highBloodPressureNum);
        bloodPressureAndSpinalCurvatureMonitorTable.setHighBloodPressureRatio(highBloodPressureRatio);
        return bloodPressureAndSpinalCurvatureMonitorTable;
    }

    public BloodPressureAndSpinalCurvatureSchoolAge buildBloodPressureAndSpinalCurvatureSchoolAge(){
        BloodPressureAndSpinalCurvatureSchoolAge bloodPressureAndSpinalCurvatureSchoolAge = new BloodPressureAndSpinalCurvatureSchoolAge();
        bloodPressureAndSpinalCurvatureSchoolAge.setAbnormalSpineCurvatureRatio(abnormalSpineCurvatureRatioStr);
        bloodPressureAndSpinalCurvatureSchoolAge.setHighBloodPressureRatio(highBloodPressureRatioStr);
        return bloodPressureAndSpinalCurvatureSchoolAge;
    }
}