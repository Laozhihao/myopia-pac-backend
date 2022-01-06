package com.wupol.myopia.base.cache;

/**
 * Redis缓存key
 *
 * @Author HaoHao
 * @Date 2020/12/26
 **/
public interface RedisConstant {

    /**
     * 所有系统权限
     */
    String ALL_PERMISSION_KEY = "auth:permission:all";

    /**
     * 用户权限资源，auth:user:permission:{userId}，如：auth:user:permission:24
     */
    String USER_PERMISSION_KEY = "auth:user:permission:%d";

    /**
     * 用户授权token，auth:user:authorization:{userId}，如：auth:user:authorization:24
     */
    String USER_AUTHORIZATION_KEY = "auth:user:authorization:%d";
    /**
     * 用户旧的授权token，auth:user:authorization_old:{userId}，如：auth:user:authorization_old:24
     */
    String USER_AUTHORIZATION_OLD_KEY = "auth:user:authorization_old:%d";

    /**
     * 学生二维码过期时间
     */
    Integer TOKEN_EXPIRE_TIME = 3600;

    /**
     * 文件导出Key
     */
    String FILE_EXPORT_LIST = "file:export:list";

    /**
     * 筛查通知
     */
    String FILE_EXPORT_NOTICE_DATA = "file:export:notice:%s-%s-%s-%s-%s-%s";

    /**
     * 筛查计划
     */
    String FILE_EXPORT_PLAN_DATA = "file:export:plan:%s-%s-%s-%s";

    /**
     * 筛查计划(学校/班级/年级)
     * 作者：钓猫的小鱼
     */
    String FILE_EXPORT_PLAN_SCHOOL_GRADE_CLASS_DATA = "file:export:plan:%s-%s-%s-%s-%s-%s";

    /**
     * 导出Excel-医院
     */
    String FILE_EXPORT_EXCEL_HOSPITAL = "file:export:excel:hospital:%s-%s";

    /**
     * 导出Excel-学生
     */
    String FILE_EXPORT_EXCEL_PLAN_STUDENT = "file:export:excel:plan:student:%s-%s-%s-%s";

    /**
     * 导出Excel-学校
     */
    String FILE_EXPORT_EXCEL_SCHOOL = "file:export:excel:school:%s-%s";

    /**
     * 导出Excel-筛查机构
     */
    String FILE_EXPORT_EXCEL_ORG = "file:export:excel:org:%s-%s";

    /**
     * 导出Excel-筛查人员
     */
    String FILE_EXPORT_EXCEL_ORG_STAFF = "file:export:excel:org:staff:%s-%s";

    /**
     * 导出Excel-学生
     */
    String FILE_EXPORT_EXCEL_STUDENT = "file:export:excel:org:staff:%d-%d-%d";

    /**
     * 导出Excel-学生预警跟踪档案
     */
    String FILE_EXPORT_EXCEL_STUDENT_WARNING_ARCHIVE = "file:export:excel:student:warning:archive:%d-%d-%d";

    /**
     * 导出PDF-区域-筛查机构
     */
    String FILE_EXPORT_PDF_DISTRICT_SCREENING = "file:export:pdf:district:screening:%s-%s-%s";

    /**
     * 导出PDF-学校-筛查机构
     */
    String FILE_EXPORT_PDF_SCHOOL_SCREENING = "file:export:pdf:school:screening:%s-%s-%s-%s";

    /**
     * 导出PDF-筛查机构
     */
    String FILE_EXPORT_PDF_ORG_SCREENING = "file:export:pdf:org:screening:%s-%s-%s";

    /**
     * 导出PDF-学校筛查
     */
    String FILE_EXPORT_PDF_ARCHIVES_SCHOOL = "file:export:pdf:archives:school:%s-%s-%s";

    /**
     * 导出PDF-机构筛查
     */
    String FILE_EXPORT_PDF_ARCHIVES_ORG = "file:export:pdf:archives:org:%s-%s-%s-%s-%s-%s";

    /**
     * 同步导出学生档案卡
     */
    String SYNC_FILE_EXPORT_PDF_ARCHIVES_ORG = "file:export:pdf:archives:student:%s";

    /**
     * 导出筛查数据excel文件
     * 作者：钓猫的小鱼
     */
    String FILE_EXPORT_EXCEL_COUNT = "file:export:excel:count:%s-%s-%s-%s-%s";
    /**
     * 导出筛查数据PDF
     * 作者：钓猫的小鱼
     */
    String FILE_EXPORT_PDF_COUNT = "file:export:pdf:count:%s-%s-%s";
    /**
     * 导出筛查数据 档案库
     * 作者：钓猫的小鱼
     */
    String FILE_EXPORT_ARCHIVES_COUNT = "file:export:pdf:count:%s-%s-%s-%s-%s-%s-%s";
    /**
     * 导出Excel-学生
     * 作者：钓猫的小鱼
     */
    String FILE_EXPORT_EXCELDATA_PLAN_STUDENT = "file:export:excelData:plan:student:%s-%s-%s-%s";

    /**
     * 导出Excel-学生筛查数据
     * 作者：钓猫的小鱼
     */
    String FILE_EXPORT_PLAN_STUDENTSCREENING = "file:export:plan:studentScreening:%s-%s-%s-%s-%s-%s";



}
