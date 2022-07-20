package com.wupol.myopia.business.aggregation.export.excel.domain;

import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import lombok.Data;

import java.util.List;

/**
 * 导出问卷实体
 *
 * @author hang.yuan 2022/7/19 15:51
 */
@Data
public class ExportQuestionnaireDTO {
    /**
     *  筛查计划ID
     */
    private Integer screeningPlanId;
    /**
     * 区域ID
     */
    private Integer districtId;
    /**
     * 学校ID
     */
    private Integer schoolId;
    /**
     * 问卷类型
     */
    private List<Integer> questionnaireType;

    /**
     * 导出类型
     * {@link ExportTypeConst}
     */
    private Integer exportType;
}
