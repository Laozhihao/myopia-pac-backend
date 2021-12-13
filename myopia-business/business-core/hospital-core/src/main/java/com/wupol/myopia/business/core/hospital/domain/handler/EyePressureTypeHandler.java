package com.wupol.myopia.business.core.hospital.domain.handler;

import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.hospital.domain.model.EyePressure;

/**
 * @author Alix
 */
public class EyePressureTypeHandler extends BaseJsonTypeHandler<EyePressure> {

    @Override
    public Class<EyePressure> getTypeClass() {
        return EyePressure.class;
    }
}
