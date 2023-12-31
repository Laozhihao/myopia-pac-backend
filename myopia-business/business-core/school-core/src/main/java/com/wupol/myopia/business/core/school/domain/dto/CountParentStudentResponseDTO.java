package com.wupol.myopia.business.core.school.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * 家长端-统计家长绑定学生
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CountParentStudentResponseDTO {

    /**
     * 总数
     */
    private Integer total;

    /**
     * 详情
     */
    private List<ParentStudentDTO> item;
}
