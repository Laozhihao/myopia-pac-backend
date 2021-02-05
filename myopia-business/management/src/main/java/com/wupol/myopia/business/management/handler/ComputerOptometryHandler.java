package com.wupol.myopia.business.management.handler;

import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;

/**
 * ComputerOptometryHandler
 */
public class ComputerOptometryHandler extends BaseJsonTypeHandler<ComputerOptometryDO> {
    @Override
    public Class<ComputerOptometryDO> getTypeClass() {
        return ComputerOptometryDO.class;
    }
}
