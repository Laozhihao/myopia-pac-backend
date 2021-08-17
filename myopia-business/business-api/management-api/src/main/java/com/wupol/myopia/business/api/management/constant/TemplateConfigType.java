package com.wupol.myopia.business.api.management.constant;

import com.wupol.myopia.base.constant.PermissionTemplateType;

import java.util.HashMap;

/**
 * 筛查机构配置Map
 *
 * @author Simple4H
 */
public class TemplateConfigType {

    public static final HashMap<Integer, Integer> TEMPLATE_TO_ORG_CONFIG_TYPE = new HashMap<>();

    /**
     * 省级
     */
    public static final Integer CONFIG_PROVINCE = 0;

    /**
     * 单点
     */
    public static final Integer CONFIG_SINGLE = 1;

    /**
     * vs666
     */
    public static final Integer CONFIG_VS666 = 2;

    /**
     * 单点+vs666
     */
    public static final Integer CONFIG_SINGLE_AND_VS666 = 3;

    static {
        TEMPLATE_TO_ORG_CONFIG_TYPE.put(CONFIG_PROVINCE, PermissionTemplateType.SCREENING_ORGANIZATION.getType());
        TEMPLATE_TO_ORG_CONFIG_TYPE.put(CONFIG_SINGLE, PermissionTemplateType.SCREENING_ORG_SINGLE.getType());
        TEMPLATE_TO_ORG_CONFIG_TYPE.put(CONFIG_VS666, PermissionTemplateType.SCREENING_ORG_VS666.getType());
        TEMPLATE_TO_ORG_CONFIG_TYPE.put(CONFIG_SINGLE_AND_VS666, PermissionTemplateType.SCREENING_ORG_SINGLE_AND_VS666.getType());
    }
}
