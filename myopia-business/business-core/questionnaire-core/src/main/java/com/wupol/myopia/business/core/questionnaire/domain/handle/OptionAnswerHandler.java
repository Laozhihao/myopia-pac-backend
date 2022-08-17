package com.wupol.myopia.business.core.questionnaire.domain.handle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wupol.myopia.base.handler.ArrayTypeHandler;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;

import java.util.List;

/**
 * @author limy
 */
public class OptionAnswerHandler extends ArrayTypeHandler<OptionAnswer> {

    public OptionAnswerHandler(Class<List<OptionAnswer>> type) {
        super(type);
    }

    @Override
    protected TypeReference<List<OptionAnswer>> specificType() {
        return new TypeReference<List<OptionAnswer>>() {
        };
    }

}
