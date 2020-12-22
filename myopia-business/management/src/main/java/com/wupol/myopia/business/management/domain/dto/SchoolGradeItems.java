package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.SchoolClass;
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
public class SchoolGradeItems {

    private Integer id;

    private Integer schoolId;

    private String name;

    private List<SchoolClass> classes;
}
