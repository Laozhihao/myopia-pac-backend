package com.wupol.myopia.business.management.constant;

/**
 * 缓存相关常量
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public class CacheKey {

    /**
     * 全部行政区域
     */
    public static final String DISTRICT = "DISTRICT";
    /**
     * 指定的行政区域
     */
    public static final String DISTRICT_CODE = "DISTRICT_CODE_%s";
    /**
     * 指定的行政区域和它的子节点
     */
    public static final String DISTRICT_PARENT_CODE = "DISTRICT_PARENT_CODE_%s";
    /**
     * 指定的省的行政区域
     */
    public static final String DISTRICT_PROVINCE_CODE = "DISTRICT_PROVINCE_CODE_%s";

    /**
     * 新增员工
     */
    public static final String LOCK_ORG_STAFF_REDIS = "org:lock.org.staff:phone_%s";

    /**
     * 新增学校
     */
    public static final String LOCK_SCHOOL_REDIS = "school:lock.school:town_code_%s";

    /**
     * 新增学生
     */
    public static final String LOCK_STUDENT_REDIS = "student:lock.student:id_card_%s";

    /**
     * 新增医院
     */
    public static final String LOCK_HOSPITAL_REDIS = "hospital:lock.student:hospital_name_%s";

    /**
     * 新增机构
     */
    public static final String LOCK_ORG_REDIS = "org:lock.org:town_code_%s";

    /**
     * 行政区域中文名
     */
    public static final String DISTRICT_CN_NAME = "district:cn.name:id_%s";

}
