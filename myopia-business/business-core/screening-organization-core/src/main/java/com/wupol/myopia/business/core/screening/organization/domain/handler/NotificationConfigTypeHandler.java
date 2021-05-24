package com.wupol.myopia.business.core.screening.organization.domain.handler;

import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;
import com.wupol.myopia.business.core.screening.organization.domain.model.NotificationConfig;

/**
 * @author Alix
 */
public class NotificationConfigTypeHandler extends BaseJsonTypeHandler<NotificationConfig> {

    @Override
    public Class<NotificationConfig> getTypeClass() {
        return NotificationConfig.class;
    }


}
