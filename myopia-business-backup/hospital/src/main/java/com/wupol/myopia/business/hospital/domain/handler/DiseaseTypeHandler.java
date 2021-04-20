package com.wupol.myopia.business.hospital.domain.handler;

import com.wupol.myopia.business.hospital.domain.model.Consultation;
import com.wupol.myopia.business.management.domain.dto.NotificationConfig;
import com.wupol.myopia.business.management.domain.handler.BaseJsonTypeHandler;

/**
 * @author Alix
 */
public class DiseaseTypeHandler extends BaseJsonTypeHandler<Consultation.Disease> {

    @Override
    public Class<Consultation.Disease> getTypeClass() {
        return Consultation.Disease.class;
    }


}
