package com.wupol.myopia.business.management.handler;

import com.wupol.myopia.business.management.domain.dos.BiometricDataDO;

/**
 * VisionData 存储的json转换handler
 */
public class BiometricDataHandler extends BaseJsonTypeHandler<BiometricDataDO> {
    @Override
    public Class<BiometricDataDO> getTypeClass() {
        return BiometricDataDO.class;
    }
}
