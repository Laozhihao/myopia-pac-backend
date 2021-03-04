package com.wupol.myopia.business.hospital.domain.handler;

import com.wupol.myopia.business.hospital.domain.model.Consultation;
import com.wupol.myopia.business.hospital.domain.model.VisionMedicalRecord;
import com.wupol.myopia.business.management.domain.handler.BaseJsonTypeHandler;

/**
 * @author Alix
 */
public class VisionMedicalRecordTypeHandler extends BaseJsonTypeHandler<VisionMedicalRecord> {

    @Override
    public Class<VisionMedicalRecord> getTypeClass() {
        return VisionMedicalRecord.class;
    }
}
