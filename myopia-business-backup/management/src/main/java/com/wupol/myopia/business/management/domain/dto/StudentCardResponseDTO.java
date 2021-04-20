package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 学生档案卡实体类
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentCardResponseDTO {

    private CardInfo info;

    private CardDetails details;
}
