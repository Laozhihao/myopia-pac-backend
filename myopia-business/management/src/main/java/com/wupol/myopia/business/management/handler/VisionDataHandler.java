package com.wupol.myopia.business.management.handler;

import com.wupol.myopia.business.management.domain.dos.VisionDataDO;

/**
 * VisionData 存储的json转换handler
 */
public class VisionDataHandler extends BaseJsonTypeHandler<VisionDataDO> {
    @Override
    public Class<VisionDataDO> getTypeClass() {
        return VisionDataDO.class;
    }
}
