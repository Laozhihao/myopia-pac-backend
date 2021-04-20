package com.wupol.myopia.business.hospital.domain.handler;

import com.wupol.myopia.business.hospital.domain.model.DiopterMedicalRecord;
import com.wupol.myopia.business.management.domain.handler.BaseJsonTypeHandler;

/**
 * @author Alix
 */
public class DiopterMedicalRecordTypeHandler extends BaseJsonTypeHandler<DiopterMedicalRecord> {

    @Override
    public Class<DiopterMedicalRecord> getTypeClass() {
        return DiopterMedicalRecord.class;
    }
}
