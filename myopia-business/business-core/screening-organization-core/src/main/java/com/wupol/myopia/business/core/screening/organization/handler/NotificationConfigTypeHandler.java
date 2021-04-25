package com.wupol.myopia.business.core.screening.organization.handler;


import com.amazonaws.services.simplesystemsmanagement.model.NotificationConfig;
import com.wupol.myopia.business.common.utils.handler.BaseJsonTypeHandler;

/**
 * @author Alix
 */
public class NotificationConfigTypeHandler extends BaseJsonTypeHandler<NotificationConfig> {

    @Override
    public Class<NotificationConfig> getTypeClass() {
        return NotificationConfig.class;
    }


}
