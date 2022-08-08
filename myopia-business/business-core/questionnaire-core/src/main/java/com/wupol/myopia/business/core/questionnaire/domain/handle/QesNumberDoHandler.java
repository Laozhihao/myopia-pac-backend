package com.wupol.myopia.business.core.questionnaire.domain.handle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wupol.myopia.base.handler.ArrayTypeHandler;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesSerialNumberDO;

import java.util.List;

/**
 * @author Simple4H
 */
public class QesNumberDoHandler extends ArrayTypeHandler<QesSerialNumberDO> {

    public QesNumberDoHandler(Class<List<QesSerialNumberDO>> type) {
        super(type);
    }

    @Override
    protected TypeReference<List<QesSerialNumberDO>> specificType() {
        return new TypeReference<List<QesSerialNumberDO>>() {
        };
    }

}
