package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 导出excel数据对象
 *
 * @author hang.yuan 2022/7/25 18:21
 */
@Data
public class ExcelDataBO {
    /**
     * 学生ID
     */
    private String studentId;
    /**
     * 学生的数据集合
     */
    private List<String> dataList;

}
