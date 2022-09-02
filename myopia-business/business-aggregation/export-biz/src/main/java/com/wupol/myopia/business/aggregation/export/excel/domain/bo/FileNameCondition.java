package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

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
public class FileNameCondition {

    /**
     * 学校ID
     */
    private Integer schoolId;
    /**
     * 区域编码
     */
    private Long districtCode;
    /**
     * 问卷类型
     */
    private Integer questionnaireType;
    /**
     * 文件类型
     */
    private String fileType;


    public FileNameCondition(Integer schoolId, Integer questionnaireType) {
        this.schoolId = schoolId;
        this.questionnaireType = questionnaireType;
    }

    public FileNameCondition(Long districtCode, Integer questionnaireType) {
        this.districtCode = districtCode;
        this.questionnaireType = questionnaireType;
    }
}
