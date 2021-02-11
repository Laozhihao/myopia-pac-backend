package com.wupol.myopia.business.management.domain.handler;

import com.wupol.myopia.business.management.domain.dto.NotificationConfig;

/**
 * @author Alix
 */
public class NotificationConfigTypeHandler extends BaseJsonTypeHandler<NotificationConfig> {

    @Override
    public Class<NotificationConfig> getTypeClass() {
        return NotificationConfig.class;
    }


}
