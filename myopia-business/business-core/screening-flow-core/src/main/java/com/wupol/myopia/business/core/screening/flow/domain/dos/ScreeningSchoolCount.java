package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;

/**
 * 筛查中的学校总数
 *
 * @Author HaoHao
 * @Date 2022/6/9
 **/
@Data
public class ScreeningSchoolCount {
    /**
     * 筛查计划ID
     */
    private Integer planId;
    /**
     * 筛查中的学校总数
     */
    private Integer schoolCount;
}
