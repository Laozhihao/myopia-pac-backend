package com.wupol.myopia.business.hospital.domain.handler;

import com.wupol.myopia.business.hospital.domain.model.Consultation;
import com.wupol.myopia.business.management.domain.handler.BaseJsonTypeHandler;

/**
 * @author Alix
 */
public class ConsultationTypeHandler extends BaseJsonTypeHandler<Consultation> {

    @Override
    public Class<Consultation> getTypeClass() {
        return Consultation.class;
    }
}
