package com.wupol.myopia.business.core.hospital.domain.handler;

import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.hospital.domain.model.Consultation;

/**
 * @author Alix
 */
public class DiseaseTypeHandler extends BaseJsonTypeHandler<Consultation.Disease> {

    @Override
    public Class<Consultation.Disease> getTypeClass() {
        return Consultation.Disease.class;
    }


}
