package com.wupol.myopia.business.management.constant;

/**
 * 缓存相关常量
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public class CacheKey {

    /** 全部行政区域 */
    public static final String DISTRICT = "DISTRICT";
    /** 指定的行政区域 */
    public static final String DISTRICT_CODE = "DISTRICT_CODE_%s";
    /** 指定的行政区域和它的子节点 */
    public static final String DISTRICT_PARENT_CODE = "DISTRICT_PARENT_CODE_%s";
    /** 指定的省的行政区域 */
    public static final String DISTRICT_PROVINCE_CODE = "DISTRICT_PROVINCE_CODE_%s";
    /** 全部行政区域的ID、Name的Map */
    public static final String DISTRICT_ID_NAME_MAP = "DISTRICT";
}
