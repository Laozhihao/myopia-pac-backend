package com.wupol.myopia.business.core.school.management.domain.dto;

import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import lombok.Getter;
import lombok.Setter;

/**
 * 学生列表
 *
 * @author Simple4H
 */
@Getter
@Setter
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
