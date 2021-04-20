package com.wupol.myopia.business.hospital.domain.handler;

import com.wupol.myopia.business.hospital.domain.model.ToscaMedicalRecord;
import com.wupol.myopia.business.management.domain.handler.BaseJsonTypeHandler;

/**
 * @author Alix
 */
public class ToscaMedicalRecordTypeHandler extends BaseJsonTypeHandler<ToscaMedicalRecord> {

    @Override
    public Class<ToscaMedicalRecord> getTypeClass() {
        return ToscaMedicalRecord.class;
    }
}
