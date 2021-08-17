package com.wupol.myopia.oauth.constant;

import com.wupol.myopia.base.constant.PermissionTemplateType;

import java.util.HashMap;

/**
 * 筛查机构配置Map
 *
 * @author Simple4H
 */
public class OrgScreeningMap {

    public static final HashMap<Integer, Integer> ORG_CONFIG_TYPE_TO_TEMPLATE = new HashMap<>();

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
        ORG_CONFIG_TYPE_TO_TEMPLATE.put(CONFIG_PROVINCE, PermissionTemplateType.SCREENING_ORGANIZATION.getType());
        ORG_CONFIG_TYPE_TO_TEMPLATE.put(CONFIG_SINGLE, PermissionTemplateType.SCREENING_ORG_SINGLE.getType());
        ORG_CONFIG_TYPE_TO_TEMPLATE.put(CONFIG_VS666, PermissionTemplateType.SCREENING_ORG_VS666.getType());
        ORG_CONFIG_TYPE_TO_TEMPLATE.put(CONFIG_SINGLE_AND_VS666, PermissionTemplateType.SCREENING_ORG_SINGLE_AND_VS666.getType());
    }
}
