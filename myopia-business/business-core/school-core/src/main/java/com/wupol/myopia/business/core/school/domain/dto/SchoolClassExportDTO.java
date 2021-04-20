package com.wupol.myopia.business.core.school.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 班级导出
 *
 * @author Simple4H
 */
@Data
public class SchoolClassExportDTO {

    @JSONField(serialize = false)
    private Integer id;

    /**
     * 年级ID
     */
    @JSONField(serialize = false)
    private Integer gradeId;

    /**
     * 学校ID
     */
    @JSONField(serialize = false)
    private Integer schoolId;

    /**
     * 班级名称
     */
    private String name;

}
