package com.wupol.myopia.business.core.common.domain.model.handle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wupol.myopia.base.handler.ArrayTypeHandler;
import com.wupol.myopia.business.core.common.domain.model.SystemCodeDO;

import java.util.List;

/**
 * @author Simple4H
 */
public class SystemCodeDOHandler extends ArrayTypeHandler<SystemCodeDO> {

    public SystemCodeDOHandler(Class<List<SystemCodeDO>> type) {
        super(type);
    }

    @Override
    protected TypeReference<List<SystemCodeDO>> specificType() {
        return new TypeReference<List<SystemCodeDO>>() {
        };
    }

}
