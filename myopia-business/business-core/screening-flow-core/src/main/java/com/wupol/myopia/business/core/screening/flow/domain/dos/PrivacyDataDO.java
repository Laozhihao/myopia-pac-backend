package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;

/**
 * 个人隐私项
 */
@Data
public class PrivacyDataDO {
    // 是否隐私项
    private Boolean hasIncident;

    // 出现的年龄
    private Integer age;
}
