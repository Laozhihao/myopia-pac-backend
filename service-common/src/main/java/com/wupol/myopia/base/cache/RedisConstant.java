package com.wupol.myopia.base.cache;

import lombok.experimental.UtilityClass;

/**
 * Redis缓存key
 *
 * @Author HaoHao
 * @Date 2020/12/26
 **/
@UtilityClass
public final class RedisConstant {

    /**
     * 所有系统权限
     */
    public static final String ALL_PERMISSION_KEY = "auth:permission:all";

    /**
     * 用户权限资源，auth:user:permission:{systemCode}:{userId}，如：auth:user:permission:1:24
     */
    public static final String USER_PERMISSION_KEY = "auth:user:permission:%d:%d";

    /**
     * 用户授权token，auth:user:authorization:{systemCode}:{userId}，如：auth:user:authorization:1:24
     */
    public static final String USER_AUTHORIZATION_KEY = "auth:user:authorization:%d:%d";
    /**
     * 用户旧的授权token，auth:user:authorization_old:{systemCode}:{userId}，如：auth:user:authorization_old:1:24
     */
    public static final String USER_AUTHORIZATION_OLD_KEY = "auth:user:authorization_old:%d:%d";

    /**
     * 学生二维码过期时间
     */
    public static final Integer TOKEN_EXPIRE_TIME = 3600;

    /**
     * 文件导出Key
     */
    public static final String FILE_EXPORT_LIST = "file:export:list:%s";

    /**
     * 筛查计划
     */
    public static final String FILE_EXPORT_PLAN_DATA = "file:export:plan:%s-%s-%s-%s";

    /**
     * 筛查计划(学校/班级/年级)
     * 作者：钓猫的小鱼
     */
    public static final String FILE_EXPORT_PLAN_SCHOOL_GRADE_CLASS_DATA = "file:export:plan:%s-%s-%s-%s-%s-%s";

    /**
     * 导出Excel-医院
     */
    public static final String FILE_EXPORT_EXCEL_HOSPITAL = "file:export:excel:hospital:%s-%s";

    /**
     * 导出Excel-学生
     */
    public static final String FILE_EXPORT_EXCEL_PLAN_STUDENT = "file:export:excel:plan:student:%s-%s-%s-%s";

    /**
     * 导出Excel-学校
     */
    public static final String FILE_EXPORT_EXCEL_SCHOOL = "file:export:excel:school:%s-%s";

    /**
     * 导出Excel-筛查机构
     */
    public static final String FILE_EXPORT_EXCEL_ORG = "file:export:excel:org:%s-%s";

    /**
     * 导出Excel-筛查人员
     */
    public static final String FILE_EXPORT_EXCEL_ORG_STAFF = "file:export:excel:org:staff:%s-%s";

    /**
     * 导出Excel-学生
     */
    public static final String FILE_EXPORT_EXCEL_STUDENT = "file:export:excel:org:staff:%d-%d-%d";

    /**
     * 导出Excel-学生预警跟踪档案
     */
    public static final String FILE_EXPORT_EXCEL_STUDENT_WARNING_ARCHIVE = "file:export:excel:student:warning:archive:%d-%d-%d";

    /**
     * 导出PDF-区域-筛查机构
     */
    public static final String FILE_EXPORT_PDF_DISTRICT_SCREENING = "file:export:pdf:district:screening:%s-%s-%s";

    /**
     * 导出PDF-学校-筛查机构
     */
    public static final String FILE_EXPORT_PDF_SCHOOL_SCREENING = "file:export:pdf:school:screening:%s-%s-%s";

    /**
     * 导出PDF-筛查机构
     */
    public static final String FILE_EXPORT_PDF_ORG_SCREENING = "file:export:pdf:org:screening:%s-%s-%s-%s";

    /**
     * 导出PDF-学校筛查
     */
    public static final String FILE_EXPORT_PDF_ARCHIVES_SCHOOL = "file:export:pdf:archives:school:%s-%s-%s";

    /**
     * 导出PDF-区域筛查
     */
    public static final String FILE_EXPORT_PDF_ARCHIVES_DISTRICT = "file:export:pdf:archives:district:%s-%s-%s-%s-%s-%s";


    /**
     * 导出PDF-机构筛查
     */
    public static final String FILE_EXPORT_PDF_ARCHIVES_ORG = "file:export:pdf:archives:org:%s-%s-%s-%s-%s-%s";

    /**
     * 同步导出学生档案卡
     */
    public static final String SYNC_FILE_EXPORT_PDF_ARCHIVES_ORG = "file:export:pdf:archives:student:%s";

    /**
     * 导出筛查数据excel文件
     * 作者：钓猫的小鱼
     */
    public static final String FILE_EXPORT_EXCEL_COUNT = "file:url:export:excel:count:%s-%s-%s-%s-%s-%s";
    /**
     * 导出档案卡
     * 作者：钓猫的小鱼
     */
    public static final String FILE_EXPORT_EXCEL_ARCHIVES_COUNT = "file:url:export:excel:archives:count:%s-%s-%s-%s-%s";

    /**
     * 导出筛查数据PDF
     * 作者：钓猫的小鱼
     */
    public static final String FILE_EXPORT_PDF_COUNT = "file:url:export:pdf:count:%s-%s-%s-%s";

    /**
     * 导出筛查数据PDF(区域)
     * 作者：钓猫的小鱼
     */
    public static final String FILE_URL_USERID_NOTIFICATIONID_DISTRICTID_COUNT= "file:url:userId:notificationId:districtId:count:%s-%s-%s-%s";
    /**
     * 导出筛查数据PDF(学校)
     * 作者：钓猫的小鱼
     */
    public static final String FILE_URL_USERID_NOTIFICATIONID_PLANID_SCHOOLID_COUNT= "file:url:userId:notificationId:planId:schoolId:count:%s-%s-%s-%s-%s";

    /**
     * 导出筛查数据PDF(筛查机构)
     * 作者：钓猫的小鱼
     */
    public static final String FILE_URL_USERID_PLANID_SCREENINGORGID_COUNT= "file:url:userId:notificationId:planId:schoolId:count:%s-%s-%s-%s";


    /**
     * 导出筛查数据 档案卡
     * 作者：钓猫的小鱼
     */
    public static final String FILE_EXPORT_ARCHIVES_COUNT = "file:url:export:pdf:count:%s-%s-%s-%s-%s-%s-%s-%s";
    /**
     * 导出Excel-学生
     * 作者：钓猫的小鱼
     */
    public static final String FILE_EXPORT_EXCELDATA_PLAN_STUDENT = "file:export:excelData:plan:student:%s-%s-%s-%s";

    /**
     * 导出Excel-学生筛查数据
     * 作者：钓猫的小鱼
     */
    public static final String FILE_EXPORT_PLAN_STUDENTSCREENING = "file:export:plan:studentScreening:%s-%s-%s-%s-%s-%s";

    /**
     * 学校端筛查数据
     */
    public static final String FILE_EXCEL_SCHOOL_PLAN = "export:file:export:plan:school:screening:%s-%s-%s";

    /**
     * 导出筛查计划
     */
    public static final String FILE_EXPORT_PLAN_SCREENING_DATA = "file:export:plan:%s-%s-%s-%s-%s-%s";

    /**
     * 导出档案卡
     */
    public static final String FILE_EXPORT_PLAN_ARCHIVES_DATA = "file:export:archives:%s-%s-%s-%s-%s-%s-%s";

    /**
     * 导出PDF-区域-筛查机构
     */
    public static final String FILE_EXPORT_PDF_QRCODE_SCREENING = "file:export:pdf:qrcode:screening:%s-%s-%s-%s-%s-%s";

}
