package com.wupol.myopia.business.core.school.domain.dto;

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
public class SchoolGradeItemsDTO {

    private Integer id;

    private Integer schoolId;

    private String gradeCode;

    private String name;

    /**
     * 唯一Id
     */
    private String uniqueId;

    private List<SchoolClassDTO> child;
}
