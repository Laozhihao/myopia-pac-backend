package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 学生-筛查次数
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentScreeningCountDTO {

    private Integer count;

    private Integer studentId;
}
