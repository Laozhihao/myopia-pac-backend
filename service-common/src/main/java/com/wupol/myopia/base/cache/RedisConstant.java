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
     * 用户权限资源，auth:user:permission:{systemCode}_{userId}，如：auth:user:permission:1_24
     */
    String USER_PERMISSION_KEY = "auth:user:permission:%d_%d";

    /**
     * 用户授权token，auth:user:authorization:{systemCode}_{userId}，如：auth:user:authorization:1_24
     */
    String USER_AUTHORIZATION_KEY = "auth:user:authorization:%d_%d";
    /**
     * 用户旧的授权token，auth:user:authorization_old:{systemCode}_{userId}，如：auth:user:authorization_old:1_24
     */
    String USER_AUTHORIZATION_OLD_KEY = "auth:user:authorization_old:%d_%d";

    /**
     * 学生二维码过期时间
     */
    Integer TOKEN_EXPIRE_TIME = 3600;

    /**
     * 文件导出Key
     */
    String FILE_EXPORT_LIST = "file:export:list";

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
    String FILE_EXPORT_PDF_SCHOOL_SCREENING = "file:export:pdf:school:screening:%s-%s-%s";

    /**
     * 导出PDF-筛查机构
     */
    String FILE_EXPORT_PDF_ORG_SCREENING = "file:export:pdf:org:screening:%s-%s-%s-%s";

    /**
     * 导出PDF-学校筛查
     */
    String FILE_EXPORT_PDF_ARCHIVES_SCHOOL = "file:export:pdf:archives:school:%s-%s-%s";

    /**
     * 导出PDF-区域筛查
     */
    String FILE_EXPORT_PDF_ARCHIVES_DISTRICT = "file:export:pdf:archives:district:%s-%s-%s-%s-%s-%s";


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
    String FILE_EXPORT_EXCEL_COUNT = "file:url:export:excel:count:%s-%s-%s-%s-%s-%s";
    /**
     * 导出档案卡
     * 作者：钓猫的小鱼
     */
    String FILE_EXPORT_EXCEL_ARCHIVES_COUNT = "file:url:export:excel:archives:count:%s-%s-%s-%s-%s";

    /**
     * 导出筛查数据PDF
     * 作者：钓猫的小鱼
     */
    String FILE_EXPORT_PDF_COUNT = "file:url:export:pdf:count:%s-%s-%s-%s";

    /**
     * 导出筛查数据PDF(区域)
     * 作者：钓猫的小鱼
     */
    String FILE_URL_USERID_NOTIFICATIONID_DISTRICTID_COUNT= "file:url:userId:notificationId:districtId:count:%s-%s-%s-%s";
    /**
     * 导出筛查数据PDF(学校)
     * 作者：钓猫的小鱼
     */
    String FILE_URL_USERID_NOTIFICATIONID_PLANID_SCHOOLID_COUNT= "file:url:userId:notificationId:planId:schoolId:count:%s-%s-%s-%s-%s";

    /**
     * 导出筛查数据PDF(筛查机构)
     * 作者：钓猫的小鱼
     */
    String FILE_URL_USERID_PLANID_SCREENINGORGID_COUNT= "file:url:userId:notificationId:planId:schoolId:count:%s-%s-%s-%s";


    /**
     * 导出筛查数据 档案卡
     * 作者：钓猫的小鱼
     */
    String FILE_EXPORT_ARCHIVES_COUNT = "file:url:export:pdf:count:%s-%s-%s-%s-%s-%s-%s-%s";
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

    /**
     * 学校端筛查数据
     */
    String FILE_EXCEL_SCHOOL_PLAN = "export:file:export:plan:school:screening:%s-%s-%s";

    /**
     * 导出筛查计划
     */
    String FILE_EXPORT_PLAN_SCREENING_DATA = "file:export:plan:%s-%s-%s-%s-%s-%s";

    /**
     * 导出档案卡
     */
    String FILE_EXPORT_PLAN_ARCHIVES_DATA = "file:export:archives:%s-%s-%s-%s-%s-%s-%s";

    /**
     * 导出PDF-区域-筛查机构
     */
    String FILE_EXPORT_PDF_QRCODE_SCREENING = "file:export:pdf:qrcode:screening:%s-%s-%s-%s-%s-%s";

    /**
     * 医院眼底影像上传
     */
    String HOSPITAL_DEVICE_UPLOAD_FUNDUS_PATIENT = "hospital:device:upload:fundus:patient:%s";

    /**
     * 导出PDF-区域-筛查机构
     */
    String FILE_EXPORT_EXCEL_SCHOOL_EYE_HEALTH = "file:export:excel:school:eye_health:%s-%s";

    /**
     * 文件导出Key
     */
    String FILE_EXPORT_ASYNC_LIST = "file:export:async:list";

    /**
     * 导出任务Key
     */
    String FILE_EXPORT_ASYNC_TASK_KEY = "file_export_async_task_key:%s";

    /**
     * 异步导出通知Key
     */
    String FILE_EXPORT_ASYNC_TASK_ERROR_NOTICE = "file_export_async_task_error_notice:%s";

    /**
     * 导出筛查数据
     */
    String IMPORT_SCHOOL_SCREENING_DATA = "import:school:screening:data:%s%s";

    /**
     * 通知关联Key
     */
    String NOTICE_LINK_LIST = "notice:link:list";

    /**
     * 通知关联异常Key
     */
    String NOTICE_LINK_ERROR_LIST = "notice:link:error:list";

}
