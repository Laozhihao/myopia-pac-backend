package com.wupol.myopia.business.core.questionnaire.domain.handle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wupol.myopia.base.handler.ArrayTypeHandler;
import com.wupol.myopia.business.core.questionnaire.domain.dos.Option;

import java.util.List;

/**
 * @author limy
 */
public class OptionHandler extends ArrayTypeHandler<Option> {

    public OptionHandler(Class<List<Option>> type) {
        super(type);
    }

    @Override
    protected TypeReference<List<Option>> specificType() {
        return new TypeReference<List<Option>>() {
        };
    }

}
