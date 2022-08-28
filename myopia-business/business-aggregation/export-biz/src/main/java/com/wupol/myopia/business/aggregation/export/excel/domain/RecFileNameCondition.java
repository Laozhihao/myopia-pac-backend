package com.wupol.myopia.business.aggregation.export.excel.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * rec文件名实体条件
 *
 * @author hang.yuan 2022/8/27 11:47
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class RecFileNameCondition {

    private Integer schoolId;
    private Long districtCode;
    private Integer questionnaireType;

    public RecFileNameCondition(Integer schoolId, Integer questionnaireType) {
        this.schoolId = schoolId;
        this.questionnaireType = questionnaireType;
    }

    public RecFileNameCondition(Long districtCode, Integer questionnaireType) {
        this.districtCode = districtCode;
        this.questionnaireType = questionnaireType;
    }
}
