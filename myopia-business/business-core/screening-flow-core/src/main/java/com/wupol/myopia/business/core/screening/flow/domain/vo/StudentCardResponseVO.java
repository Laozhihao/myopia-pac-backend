package com.wupol.myopia.business.core.screening.flow.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 学生档案卡实体类
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentCardResponseVO {

    /**
     * 学生档案卡基本信息
     */
    private CardInfoVO info;

    /**
     * 学生档案卡视力详情
     */
    private CardDetailsVO details;

    /**
     * 海南省学生眼疾病筛查单
     */
    private HaiNanCardDetail haiNanCardDetail;

    /**
     * 0-幼儿园 1-中学生
     */
    private Integer status;
}
