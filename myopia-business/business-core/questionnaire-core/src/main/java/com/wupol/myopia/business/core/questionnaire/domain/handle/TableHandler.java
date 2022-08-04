package com.wupol.myopia.business.core.questionnaire.domain.handle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wupol.myopia.base.handler.ArrayTypeHandler;
import com.wupol.myopia.business.core.questionnaire.domain.dos.Table;

import java.util.List;

/**
 * @author Simple4H
 */
public class TableHandler extends ArrayTypeHandler<Table> {

    public TableHandler(Class<List<Table>> type) {
        super(type);
    }

    @Override
    protected TypeReference<List<Table>> specificType() {
        return new TypeReference<List<Table>>() {
        };
    }

}
