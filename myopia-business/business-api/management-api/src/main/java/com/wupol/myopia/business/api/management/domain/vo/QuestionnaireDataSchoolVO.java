package com.wupol.myopia.business.api.management.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 问卷数据学校
 *
 * @author hang.yuan 2022/7/21 00:20
 */
@Data
@AllArgsConstructor
public class QuestionnaireDataSchoolVO {
    /**
     * 学校ID
     */
    private Integer schoolId;
    /**
     * 学校名称
     */
    private String schoolName;
}
