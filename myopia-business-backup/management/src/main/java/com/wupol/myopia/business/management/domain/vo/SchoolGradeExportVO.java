package com.wupol.myopia.business.management.domain.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 班级返回体
 *
 * @author Simple4H
 */
@Setter
@Getter
public class SchoolGradeExportVO {

    @JSONField(serialize = false)
    private Integer id;

    @JSONField(serialize = false)
    private Integer schoolId;

    @JSONField(serialize = false)
    private String schoolNo;

    @JSONField(serialize = false)
    private String gradeCode;

    @JSONField(ordinal = 1)
    private String name;

    @JSONField(ordinal = 2, name = "班级信息")
    private List<SchoolClassExportVO> child;
}
