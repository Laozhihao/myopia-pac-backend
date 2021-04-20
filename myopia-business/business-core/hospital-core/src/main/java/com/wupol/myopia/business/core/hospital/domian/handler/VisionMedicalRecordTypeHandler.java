package com.wupol.myopia.business.core.hospital.domian.handler;

import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.hospital.domian.model.VisionMedicalRecord;

/**
 * @author Alix
 */
public class VisionMedicalRecordTypeHandler extends BaseJsonTypeHandler<VisionMedicalRecord> {

    @Override
    public Class<VisionMedicalRecord> getTypeClass() {
        return VisionMedicalRecord.class;
    }
}
