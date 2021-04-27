package com.wupol.myopia.business.core.stat.handler;


import com.wupol.myopia.business.management.domain.dos.BigScreenScreeningDO;
import com.wupol.myopia.business.management.domain.handler.BaseJsonTypeHandler;

/**
 * @author
 * 自定义json转换器
 */
public class BigScreenScreeningDOHandler extends BaseJsonTypeHandler<BigScreenScreeningDO> {
    @Override
    public Class<BigScreenScreeningDO> getTypeClass() {
        return BigScreenScreeningDO.class;
    }
}