package com.wupol.myopia.business.core.school.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 学生统计
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentCountDTO {

    /**
     * 学校编号
     */
    private Integer schoolId;

    /**
     * 学生统计
     */
    private Integer count;
}
