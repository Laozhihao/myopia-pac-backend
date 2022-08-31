package com.wupol.myopia.business.core.questionnaire.domain.handle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wupol.myopia.base.handler.ArrayTypeHandler;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesDataDO;

import java.util.List;

/**
 * @author Simple4H
 */
public class QesDataDoHandler extends ArrayTypeHandler<QesDataDO> {

    public QesDataDoHandler(Class<List<QesDataDO>> type) {
        super(type);
    }

    @Override
    protected TypeReference<List<QesDataDO>> specificType() {
        return new TypeReference<List<QesDataDO>>() {
        };
    }

}
