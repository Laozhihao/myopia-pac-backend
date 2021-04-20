package com.wupol.myopia.business.core.hospital.domian.handler;

import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.hospital.domian.model.Consultation;

/**
 * @author Alix
 */
public class ConsultationTypeHandler extends BaseJsonTypeHandler<Consultation> {

    @Override
    public Class<Consultation> getTypeClass() {
        return Consultation.class;
    }
}
