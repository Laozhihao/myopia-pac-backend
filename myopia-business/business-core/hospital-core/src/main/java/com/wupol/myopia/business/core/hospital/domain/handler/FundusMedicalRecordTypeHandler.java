package com.wupol.myopia.business.core.hospital.domain.handler;

import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.hospital.domain.model.FundusMedicalRecord;

/**
 * @author Simple4H
 */
public class FundusMedicalRecordTypeHandler extends BaseJsonTypeHandler<FundusMedicalRecord> {

    @Override
    public Class<FundusMedicalRecord> getTypeClass() {
        return FundusMedicalRecord.class;
    }
}
