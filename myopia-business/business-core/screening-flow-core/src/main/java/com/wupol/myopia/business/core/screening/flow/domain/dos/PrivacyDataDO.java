package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;

/**
 * @Description 个人隐私项
 * @Date 2021/4/06 16:50
 * @Author by xz
 */
@Data
public class PrivacyDataDO {
    /**
     * 是否隐私项
     */
    private Boolean hasIncident;

    /**
     * 出现的年龄
     */
    private Integer age;
}
