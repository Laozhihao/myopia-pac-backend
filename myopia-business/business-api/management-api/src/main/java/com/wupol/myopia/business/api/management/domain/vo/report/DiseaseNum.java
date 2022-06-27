package com.wupol.myopia.business.api.management.domain.vo.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.service.report.EntityFunction;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DiseaseNumDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 疾病统计
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DiseaseNum extends EntityFunction {

    private Integer validScreeningNum;
    /**
     * 贫血
     */
    private Integer anemia;
    /**
     * 高血压
     */
    private Integer hypertension;

    /**
     * 糖尿病
     */
    private Integer diabetes;
    /**
     * 过敏性哮喘
     */
    private Integer allergicAsthma;
    /**
     * 身体残疾
     */
    private Integer physicalDisability;

    // ========== 不带% ============
    /**
     * 贫血
     */
    private BigDecimal anemiaRatio;
    /**
     * 高血压
     */
    private BigDecimal hypertensionRatio;

    /**
     * 糖尿病
     */
    private BigDecimal diabetesRatio;
    /**
     * 过敏性哮喘
     */
    private BigDecimal allergicAsthmaRatio;
    /**
     * 身体残疾
     */
    private BigDecimal physicalDisabilityRatio;

    // ========== 带% ============
    /**
     * 贫血
     */
    private String anemiaRatioStr;
    /**
     * 高血压
     */
    private String hypertensionRatioStr;

    /**
     * 糖尿病
     */
    private String diabetesRatioStr;
    /**
     * 过敏性哮喘
     */
    private String allergicAsthmaRatioStr;
    /**
     * 身体残疾
     */
    private String physicalDisabilityRatioStr;

    public DiseaseNum build(List<StatConclusion> statConclusionList) {
        this.validScreeningNum = statConclusionList.size();
        this.anemia = getSum(statConclusionList, DiseaseNumDO::getAnemia);
        this.hypertension = getSum(statConclusionList, DiseaseNumDO::getHypertension);
        this.diabetes = getSum(statConclusionList, DiseaseNumDO::getDiabetes);
        this.allergicAsthma = getSum(statConclusionList, DiseaseNumDO::getAllergicAsthma);
        this.physicalDisability = getSum(statConclusionList, DiseaseNumDO::getPhysicalDisability);
        return this;
    }

    /**
     * 不带%
     */
    public DiseaseNum ratioNotSymbol() {
        this.anemiaRatio = getRatioNotSymbol(anemia, getTotal());
        this.hypertensionRatio = getRatioNotSymbol(hypertension, getTotal());
        this.diabetesRatio = getRatioNotSymbol(diabetes, getTotal());
        this.allergicAsthmaRatio = getRatioNotSymbol(allergicAsthma, getTotal());
        this.physicalDisabilityRatio = getRatioNotSymbol(physicalDisability, getTotal());
        return this;
    }

    /**
     * 带%
     */
    public DiseaseNum ratio() {
        this.anemiaRatioStr = getRatio(anemia, getTotal());
        this.hypertensionRatioStr = getRatio(hypertension, getTotal());
        this.diabetesRatioStr = getRatio(diabetes, getTotal());
        this.allergicAsthmaRatioStr = getRatio(allergicAsthma, getTotal());
        this.physicalDisabilityRatioStr = getRatio(physicalDisability, getTotal());
        return this;
    }

    private Integer getSum(List<StatConclusion> statConclusionList, Function<DiseaseNumDO, Integer> function) {
        if (CollectionUtil.isNotEmpty(statConclusionList)) {
            return statConclusionList.stream().map(StatConclusion::getDiseaseNum).filter(Objects::nonNull)
                    .map(function).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        }
        return ReportConst.ZERO;
    }

    private static Integer getTotal(){
        return MAP.get(0);
    }

    public static final Map<Integer,Integer> MAP = Maps.newConcurrentMap();

    public DistrictDiseaseMonitorVO.DiseaseMonitorVariableVO buildDiseaseMonitorVariableVO(){
        DistrictDiseaseMonitorVO.DiseaseMonitorVariableVO diseaseMonitorVariableVO = new DistrictDiseaseMonitorVO.DiseaseMonitorVariableVO();
        diseaseMonitorVariableVO.setHypertensionRatio(hypertensionRatioStr);
        diseaseMonitorVariableVO.setAnemiaRatio(anemiaRatioStr);
        diseaseMonitorVariableVO.setDiabetesRatio(diabetesRatioStr);
        diseaseMonitorVariableVO.setAllergicAsthmaRatio(allergicAsthmaRatioStr);
        diseaseMonitorVariableVO.setPhysicalDisabilityRatio(physicalDisabilityRatioStr);
        return diseaseMonitorVariableVO;
    }

    public DistrictDiseaseMonitorVO.DiseaseMonitorTable buildTable() {
        DistrictDiseaseMonitorVO.DiseaseMonitorTable diseaseMonitorTable = new DistrictDiseaseMonitorVO.DiseaseMonitorTable();
        diseaseMonitorTable.setValidScreeningNum(validScreeningNum);
        diseaseMonitorTable.setHypertensionNum(hypertension);
        diseaseMonitorTable.setHypertensionRatio(hypertensionRatio);
        diseaseMonitorTable.setAnemiaNum(anemia);
        diseaseMonitorTable.setAnemiaRatio(anemiaRatio);
        diseaseMonitorTable.setDiabetesNum(diabetes);
        diseaseMonitorTable.setDiabetesRatio(diabetesRatio);
        diseaseMonitorTable.setAllergicAsthmaNum(allergicAsthma);
        diseaseMonitorTable.setAllergicAsthmaRatio(allergicAsthmaRatio);
        diseaseMonitorTable.setPhysicalDisabilityNum(physicalDisability);
        diseaseMonitorTable.setPhysicalDisabilityRatio(physicalDisabilityRatio);
        return diseaseMonitorTable;
    }

}