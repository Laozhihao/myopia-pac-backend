package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 生成数据条件
 *
 * @author hang.yuan 2022/8/17 09:56
 */
@Data
@Accessors(chain = true)
public class GenerateDataCondition {
    /**
     * 主问卷类型
     */
    private QuestionnaireTypeEnum mainBodyType;
    /**
     * 基础信息问卷类型
     */
    private QuestionnaireTypeEnum baseInfoType;
    /**
     * 学龄集合
     */
    private List<Integer> gradeTypeList;
    /**
     * 导出条件
     */
    private ExportCondition exportCondition;
    /**
     * 是否记分
     */
    private Boolean isScore;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 导出问卷类型
     */
    private String fileType;
}
