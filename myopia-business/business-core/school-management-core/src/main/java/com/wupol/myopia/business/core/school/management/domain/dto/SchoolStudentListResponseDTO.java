package com.wupol.myopia.business.core.school.management.domain.dto;

import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生列表
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SchoolStudentListResponseDTO extends SchoolStudent {

    /**
     * 就诊次数
     */
    private Integer numOfVisits;

    /**
     * 筛查次数
     */
    private Integer screeningCount;

}
