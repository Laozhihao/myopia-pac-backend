package com.wupol.myopia.business.api.management.domain.vo.report;

import cn.hutool.core.collection.CollUtil;
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

/**
 * 身高体重监测统计
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HeightAndWeightNum extends EntityFunction implements Num {

    /**
     * 筛查人数
     */
    private Integer validScreeningNum;
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


    //============= 不带% ============
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


    //============= 带% ============
    /**
     * 超重率
     */
    private String overweightRatioStr;
    /**
     * 肥胖率
     */
    private String obeseRatioStr;

    /**
     * 营养不良率
     */
    private String malnourishedRatioStr;

    /**
     * 生长迟缓率
     */
    private String stuntingRatioStr;

    /**
     * 性别
     */
    private Integer gender;


    public HeightAndWeightNum build(List<StatConclusion> statConclusionList) {
        if (CollUtil.isEmpty(statConclusionList)) {
            this.validScreeningNum = ReportConst.ZERO;
            this.overweightNum = ReportConst.ZERO;
            this.obeseNum = ReportConst.ZERO;
            this.malnourishedNum = ReportConst.ZERO;
            this.stuntingNum = ReportConst.ZERO;
            return this;
        }
        this.validScreeningNum = statConclusionList.size();
        this.overweightNum = getCount(statConclusionList, StatConclusion::getIsOverweight);
        this.obeseNum = getCount(statConclusionList, StatConclusion::getIsObesity);
        this.malnourishedNum = getCount(statConclusionList, StatConclusion::getIsMalnutrition);
        this.stuntingNum = getCount(statConclusionList, StatConclusion::getIsStunting);
        return this;
    }

    /**
     * 不带%
     */
    public HeightAndWeightNum ratioNotSymbol() {
        this.overweightRatio = getRatioNotSymbol(overweightNum, getTotal());
        this.obeseRatio = getRatioNotSymbol(obeseNum, getTotal());
        this.stuntingRatio = getRatioNotSymbol(stuntingNum, getTotal());
        this.malnourishedRatio = getRatioNotSymbol(malnourishedNum, getTotal());
        return this;
    }

    /**
     * 带%
     */
    public HeightAndWeightNum ratio() {
        this.overweightRatioStr = getRatio(overweightNum, getTotal());
        this.obeseRatioStr = getRatio(obeseNum, getTotal());
        this.stuntingRatioStr = getRatio(stuntingNum, getTotal());
        this.malnourishedRatioStr = getRatio(malnourishedNum, getTotal());
        return this;
    }


    public HeightAndWeightNum setGender(Integer gender) {
        this.gender = gender;
        return this;
    }


    private static Integer getTotal(){
        return MAP.get(0);
    }

    public static final Map<Integer,Integer> MAP = Maps.newConcurrentMap();

    public HeightAndWeightMonitorTable buildTable(){
        HeightAndWeightMonitorTable heightAndWeightMonitorTable= new HeightAndWeightMonitorTable();
        heightAndWeightMonitorTable.setValidScreeningNum(validScreeningNum);
        heightAndWeightMonitorTable.setOverweightNum(overweightNum);
        heightAndWeightMonitorTable.setOverweightRatio(overweightRatio);
        heightAndWeightMonitorTable.setObeseNum(obeseNum);
        heightAndWeightMonitorTable.setObeseRatio(obeseRatio);
        heightAndWeightMonitorTable.setStuntingNum(stuntingNum);
        heightAndWeightMonitorTable.setStuntingRatio(stuntingRatio);
        heightAndWeightMonitorTable.setMalnourishedNum(malnourishedNum);
        heightAndWeightMonitorTable.setMalnourishedRatio(malnourishedRatio);
        return heightAndWeightMonitorTable;
    }

    public HeightAndWeightMonitorVariableVO buildHeightAndWeightMonitorVariableVO(){
        HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO = new HeightAndWeightMonitorVariableVO();
        heightAndWeightMonitorVariableVO.setOverweightRatio(overweightRatioStr);
        heightAndWeightMonitorVariableVO.setObeseRatio(obeseRatioStr);
        heightAndWeightMonitorVariableVO.setStuntingRatio(stuntingRatioStr);
        heightAndWeightMonitorVariableVO.setMalnourishedRatio(malnourishedRatioStr);
        return heightAndWeightMonitorVariableVO;
    }

    public HeightAndWeightSchoolAge buildHeightAndWeightSchoolAge(){
        HeightAndWeightSchoolAge heightAndWeightSchoolAge = new HeightAndWeightSchoolAge();
        heightAndWeightSchoolAge.setOverweightRatio(overweightRatioStr);
        heightAndWeightSchoolAge.setObeseRatio(obeseRatioStr);
        heightAndWeightSchoolAge.setStuntingRatio(stuntingRatioStr);
        heightAndWeightSchoolAge.setMalnourishedRatio(malnourishedRatioStr);
        return heightAndWeightSchoolAge;
    }

}