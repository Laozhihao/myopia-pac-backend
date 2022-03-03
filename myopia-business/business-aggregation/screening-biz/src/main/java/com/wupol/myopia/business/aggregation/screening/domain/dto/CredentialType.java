package com.wupol.myopia.business.aggregation.screening.domain.dto;

import com.wupol.myopia.business.common.utils.util.TwoTuple;
import org.apache.commons.lang3.StringUtils;

/**
 * @Classname StudentIdCardOrPassportType
 * @Description 证件号码
 * @Date 2022/2/25 10:58 上午
 * @Author Jacob
 * @Version
 */
public enum CredentialType {
    /**
     * 身份证
     */
    ID_CARD,
    /**
     * 护照
     */
    PASSPORT;
}
