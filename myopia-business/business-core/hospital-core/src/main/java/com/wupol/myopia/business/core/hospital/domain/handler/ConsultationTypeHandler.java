package com.wupol.myopia.business.core.hospital.domain.handler;

import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.hospital.domain.model.Consultation;

/**
 * @author Alix
 */
public class ConsultationTypeHandler extends BaseJsonTypeHandler<Consultation> {

    @Override
    public Class<Consultation> getTypeClass() {
        return Consultation.class;
    }
}
