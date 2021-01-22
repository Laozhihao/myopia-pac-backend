package com.wupol.myopia.business.management.constant;

/**
 * 缓存相关常量
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public interface CacheKey {

    /**
     * 全部行政区域
     */
    String DISTRICT = "DISTRICT";
    /**
     * 指定的行政区域
     */
    String DISTRICT_CODE = "DISTRICT_CODE_%s";
    /**
     * 指定的行政区域和它的子节点
     */
    String DISTRICT_PARENT_CODE = "DISTRICT_PARENT_CODE_%s";
    /**
     * 指定的省的行政区域
     */
    String DISTRICT_PROVINCE_CODE = "DISTRICT_PROVINCE_CODE_%s";

    /**
     * 新增员工
     */
    String LOCK_ORG_STAFF_REDIS = "lock:org_staff:phone_%s";

    /**
     * 新增学校
     */
    String LOCK_SCHOOL_REDIS = "lock:school:town_code_%s";

    /**
     * 新增学生
     */
    String LOCK_STUDENT_REDIS = "lock:student:id_card_%s";

    /**
     * 新增医院
     */
    String LOCK_HOSPITAL_REDIS = "lock:hospital:hospital_name_%s";

    /**
     * 新增机构
     */
    String LOCK_ORG_REDIS = "lock:org:town_code_%s";

    /**
     * 行政区域中文名
     */
    String DISTRICT_CN_NAME = "district:cn_name:id_%s";

}
