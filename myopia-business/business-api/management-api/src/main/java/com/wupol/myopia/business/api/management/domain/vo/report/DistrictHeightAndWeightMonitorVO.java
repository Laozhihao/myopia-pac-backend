package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 身高体重监测实体
 *
 * @author hang.yuan 2022/5/16 18:31
 */
@Data
public class DistrictHeightAndWeightMonitorVO {

    /**
     * 说明
     */
    private HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO;

    /**
     * 身高体重监测 - 不同性别
     */
    private HeightAndWeightSexVO heightAndWeightSexVO;

    /**
     * 身高体重监测 - 不同学龄段
     */
    private HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO;
    /**
     * 身高体重监测 - 不同年龄
     */
    private HeightAndWeightAgeVO heightAndWeightAgeVO;



}
