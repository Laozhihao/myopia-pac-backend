package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 可导出的学校
 *
 * @Author TaoShuai
 * @Date 2022/3/9 10:40
 * @Version 1.0
 **/

@Data
@Accessors(chain = true)
public class ExportPlanSchool implements Serializable {

    /**
     * 筛查结果--所属的计划id
     */
    private Integer screeningPlanId;

    /**
     * 筛查结果--学校id
     */
    private Integer schoolId;

}
