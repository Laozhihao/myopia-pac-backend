package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 身高体重监测实体
 *
 * @author hang.yuan 2022/5/16 18:31
 */
@Data
public class SchoolHeightAndWeightMonitorVO {

    /**
     * 说明
     */
    private HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO;

    /**
     * 身高体重监测 - 不同性别
     */
    private HeightAndWeightSexVO heightAndWeightSexVO;

    /**
     * 身高体重监测 - 不同年级
     */
    private HeightAndWeightGradeVO heightAndWeightGradeVO;
    /**
     * 身高体重监测 - 不同年龄段
     */
    private HeightAndWeightAgeVO heightAndWeightAgeVO;



}
