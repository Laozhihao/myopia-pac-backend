package com.wupol.myopia.business.core.hospital.domian.handler;

import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.hospital.domian.model.DiopterMedicalRecord;

/**
 * @author Alix
 */
public class DiopterMedicalRecordTypeHandler extends BaseJsonTypeHandler<DiopterMedicalRecord> {

    @Override
    public Class<DiopterMedicalRecord> getTypeClass() {
        return DiopterMedicalRecord.class;
    }
}
