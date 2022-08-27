package com.wupol.myopia.business.aggregation.export.excel.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * rec文件名实体条件
 *
 * @author hang.yuan 2022/8/27 11:47
 */
@Data
@Accessors(chain = true)
public class RecFileNameBO {

    private Integer schoolId;
    private Long districtCode;
    private Integer questionnaireType;
}
