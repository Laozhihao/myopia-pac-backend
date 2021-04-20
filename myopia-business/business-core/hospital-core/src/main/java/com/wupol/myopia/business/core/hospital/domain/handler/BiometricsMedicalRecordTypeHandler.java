package com.wupol.myopia.business.core.hospital.domain.handler;

import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.hospital.domain.model.BiometricsMedicalRecord;

/**
 * @author Alix
 */
public class BiometricsMedicalRecordTypeHandler extends BaseJsonTypeHandler<BiometricsMedicalRecord> {

    @Override
    public Class<BiometricsMedicalRecord> getTypeClass() {
        return BiometricsMedicalRecord.class;
    }
}
