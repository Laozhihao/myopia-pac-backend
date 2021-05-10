package com.wupol.myopia.business.hospital.domain.handler;

import com.wupol.myopia.business.hospital.domain.model.BiometricsMedicalRecord;
import com.wupol.myopia.business.management.domain.handler.BaseJsonTypeHandler;

/**
 * @author Alix
 */
public class BiometricsMedicalRecordTypeHandler extends BaseJsonTypeHandler<BiometricsMedicalRecord> {

    @Override
    public Class<BiometricsMedicalRecord> getTypeClass() {
        return BiometricsMedicalRecord.class;
    }
}
