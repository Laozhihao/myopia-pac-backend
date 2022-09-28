package com.wupol.myopia.business.core.school.domain.handle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wupol.myopia.base.handler.ArrayTypeHandler;
import com.wupol.myopia.business.core.school.domain.dos.AccountInfo;

import java.util.List;

/**
 * @author Simple4H
 */
public class AccountInfoHandler extends ArrayTypeHandler<AccountInfo> {

    public AccountInfoHandler(Class<List<AccountInfo>> type) {
        super(type);
    }

    @Override
    protected TypeReference<List<AccountInfo>> specificType() {
        return new TypeReference<List<AccountInfo>>() {
        };
    }

}
