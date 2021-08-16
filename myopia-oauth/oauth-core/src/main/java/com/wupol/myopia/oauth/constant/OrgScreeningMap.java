package com.wupol.myopia.oauth.constant;

import java.util.HashMap;

/**
 * 筛查机构配置Map
 *
 * @author Simple4H
 */
public class OrgScreeningMap {

    public static final HashMap<Integer, Integer> ORG_CONFIG_TYPE = new HashMap<>();

    /**
     * 单点
     */
    private static final Integer CONFIG_SINGLE = 1;

    /**
     * vs666
     */
    private static final Integer CONFIG_VS666 = 2;

    /**
     * 单点+vs666
     */
    private static final Integer CONFIG_SINGLE_AND_VS666 = 3;

    /**
     * 模板7-单点
     */
    private static final Integer TEMPLATE_7 = 7;

    /**
     * 模板8-vs666
     */
    private static final Integer TEMPLATE_8 = 8;

    /**
     * 模板9-单点+vs666
     */
    private static final Integer TEMPLATE_9 = 9;

    static {
        ORG_CONFIG_TYPE.put(CONFIG_SINGLE, TEMPLATE_7);
        ORG_CONFIG_TYPE.put(CONFIG_VS666, TEMPLATE_8);
        ORG_CONFIG_TYPE.put(CONFIG_SINGLE_AND_VS666, TEMPLATE_9);
    }
}
