package com.wupol.myopia.business.core.hospital.domain.handler;

import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.hospital.domain.model.ToscaMedicalRecord;

/**
 * @author Alix
 */
public class ToscaMedicalRecordTypeHandler extends BaseJsonTypeHandler<ToscaMedicalRecord> {

    @Override
    public Class<ToscaMedicalRecord> getTypeClass() {
        return ToscaMedicalRecord.class;
    }
}
