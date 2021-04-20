package com.wupol.myopia.business.core.hospital.domain.handler;

import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.hospital.domain.model.DiopterMedicalRecord;

/**
 * @author Alix
 */
public class DiopterMedicalRecordTypeHandler extends BaseJsonTypeHandler<DiopterMedicalRecord> {

    @Override
    public Class<DiopterMedicalRecord> getTypeClass() {
        return DiopterMedicalRecord.class;
    }
}
