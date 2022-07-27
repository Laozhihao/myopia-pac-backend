package com.wupol.myopia.business.core.questionnaire.domain.handle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wupol.myopia.base.handler.ArrayTypeHandler;
import com.wupol.myopia.business.core.questionnaire.domain.dos.JumpIdsDO;

import java.util.List;

/**
 * @author limy
 */
public class JumpIdsDoHandler extends ArrayTypeHandler<JumpIdsDO> {

    public JumpIdsDoHandler(Class<List<JumpIdsDO>> type) {
        super(type);
    }

    @Override
    protected TypeReference<List<JumpIdsDO>> specificType() {
        return new TypeReference<List<JumpIdsDO>>() {
        };
    }

}
