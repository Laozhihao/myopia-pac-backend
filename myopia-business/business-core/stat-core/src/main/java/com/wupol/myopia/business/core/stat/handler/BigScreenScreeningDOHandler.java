package com.wupol.myopia.business.core.stat.handler;


import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.stat.domain.dos.BigScreenScreeningDO;

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