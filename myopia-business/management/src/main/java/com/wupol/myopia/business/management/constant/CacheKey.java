package com.wupol.myopia.business.management.constant;

/**
 * 缓存相关常量
 * - 为了方便维护和便于Redis可视化工具中排查问题，采用冒号来分割风格
 * - 格式 = 类别:描述(或类别，下划线命名):唯一值描述_唯一值占位符
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public interface CacheKey {

    /**
     * 全部行政区域列表集合
     */
    String DISTRICT_ALL_LIST = "district:all:list";
    String DISTRICT_ALL_TREE = "district:all:tree";
    String DISTRICT_ALL_PROVINCE_TREE = "district:all:province_tree";

    /**
     * 指定的行政区域
     */
    String DISTRICT_LIST = "district:list:code_%s";
    /**
     * 指定的行政区域和它的子节点区域树
     */
    String DISTRICT_TREE = "district:tree:code_%s";
    String DISTRICT_CHILD_TREE = "district:child:parent_code_%s";
    /**
     * 指定的省的行政区域
     */
    String DISTRICT_PROVINCE_LIST = "district:province_list:code_%s";
    String DISTRICT_PROVINCE_TREE = "district:province_tree:code_%s";

    /**
     * 新增员工
     */
    String LOCK_ORG_STAFF_REDIS = "lock:org_staff:phone_%s";

    /**
     * 新增学校
     */
    String LOCK_SCHOOL_REDIS = "lock:school:school_no_%s";

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
    String LOCK_ORG_REDIS = "lock:org:name_%s";

    /**
     * 全部行政区域的ID、Name的Map
     */
    String DISTRICT_ID_NAME_MAP = "district:all:id_name_map";

    String DISTRICT_TOP_CN_NAME = "district:top_cn_name:code_%s";

    String DISTRICT_CN_NAME = "district:cn_name:code_%s";

    String DISTRICT_CODE = "district:name:code_%s";

    /**
     * 文件访问地址
     */
    String FILE_URL = "file:url:key_%s";

    /**
     * 短信验证码token
     */
    String SMS_CODE_TOKEN = "sms:code:token:phone_%s";
    /**
     * 短信校验失败数量
     */
    String SMS_TOKEN_FAIL_COUNT = "sms:code:fail_count:phone_%s";
}
