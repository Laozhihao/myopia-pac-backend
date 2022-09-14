package com.wupol.myopia.business.api.school.management.domain.dto;

import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生列表查询条件对象
 *
 * @author hang.yuan 2022/9/13 17:28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StudentListDTO extends PageRequest {

    /**
     * 名称
     */
    private String name;
    /**
     * 学号
     */
    private String sno;

    /**
     * 年级Id
     */
    private Integer gradeId;

    /**
     * 班级Id
     */
    private Integer classId;
}
