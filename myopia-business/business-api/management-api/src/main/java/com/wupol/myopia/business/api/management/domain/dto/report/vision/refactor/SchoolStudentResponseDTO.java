package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import lombok.Getter;
import lombok.Setter;

/**
 * 报告学生类型
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolStudentResponseDTO {

    /**
     * 是否存在幼儿园
     */
    private Boolean isHaveKindergarten;

    /**
     * 是否存在小学
     */
    private Boolean isHavePrimary;
}
