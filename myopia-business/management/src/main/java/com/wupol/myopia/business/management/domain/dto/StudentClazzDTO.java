package com.wupol.myopia.business.management.domain.dto;

import lombok.Data;

/**
 * @Description
 * @Date 2021/2/1 0:19
 * @Author by Jacob
 */
@Data
public class StudentClazzDTO {
    private Integer schoolId;
    private String schoolName;
    private Integer clazzId;
    private String clazzName;
    private Integer gradeId;
    private String gradeName;
    private Integer planId;
}
