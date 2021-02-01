package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 筛查端-记录详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class RecordDetail {

    private Integer id;

    private String schoolName;

    private Integer planScreeningNumbers;

    private Integer realScreeningNumbers;

}
