package com.wupol.myopia.business.management.handler;

import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.management.domain.dto.VisionDataDTO;

/**
 * ComputerOptometryHandler
 */
public class ComputerOptometryHandler extends BaseJsonTypeHandler<ComputerOptometryDO> {
    @Override
    public Class<ComputerOptometryDO> getTypeClass() {
        return ComputerOptometryDO.class;
    }
}
