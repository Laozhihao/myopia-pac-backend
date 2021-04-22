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

    private CardInfoVO info;

    private CardDetailsVO details;
}
