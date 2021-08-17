package com.wupol.myopia.business.api.management.constant;

import java.util.HashMap;

/**
 * 筛查机构配置Map
 *
 * @author Simple4H
 */
public class TemplateConfigType {

    public static final HashMap<Integer, Integer> TEMPLATE_TO_ORG_CONFIG_TYPE = new HashMap<>();

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

    /**
     * 模板7-单点
     */
    public static final Integer TEMPLATE_7 = 7;

    /**
     * 模板8-vs666
     */
    public static final Integer TEMPLATE_8 = 8;

    /**
     * 模板9-单点+vs666
     */
    public static final Integer TEMPLATE_9 = 9;

    static {
        TEMPLATE_TO_ORG_CONFIG_TYPE.put(TEMPLATE_7, CONFIG_SINGLE);
        TEMPLATE_TO_ORG_CONFIG_TYPE.put(TEMPLATE_8, CONFIG_VS666);
        TEMPLATE_TO_ORG_CONFIG_TYPE.put(TEMPLATE_9, CONFIG_SINGLE_AND_VS666);
    }
}
