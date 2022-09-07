package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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

    private Date updateTime;
}
