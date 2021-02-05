package com.wupol.myopia.business.management.handler;

import com.wupol.myopia.business.management.domain.dos.OtherEyeDiseasesDO;

/**
 * OtherDiseasesHandler
 */
public class OtherDiseasesHandler extends BaseJsonTypeHandler<OtherEyeDiseasesDO> {
    @Override
    public Class<OtherEyeDiseasesDO> getTypeClass() {
        return OtherEyeDiseasesDO.class;
    }
}
