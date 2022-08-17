package com.wupol.myopia.business.common.utils.constant;

import lombok.experimental.UtilityClass;

/**
 * 导出类型常量
 *
 * @author Simple4H
 */
@UtilityClass
public final class ExportTypeConst {

    /**
     * 计划
     */
    public static final Integer PLAN = 1;

    /**
     * 学校
     */
    public static final Integer SCHOOL = 2;

    /**
     * 年级
     */
    public static final Integer GRADE = 3;

    /**
     * 班级
     */
    public static final Integer CLASS = 4;

    /**
     * 区域
     */
    public static final Integer DISTRICT = 5;

    /**
     * 问卷
     */
    public static final Integer QUESTIONNAIRE = 6;

    // ===============问卷模块=================

    /**
     * 工作台->机构筛查记录【问卷数据】
     */
    public static final Integer SCREENING_RECORD = 10;

    /**
     * 工作台-问卷管理【页面级按钮：下载问卷数据】
     */
    public static final Integer QUESTIONNAIRE_PAGE = 11;
    /**
     * 工作台-问卷管理【学校列表操作：下载问卷数据】
     */
    public static final Integer QUESTIONNAIRE_SCHOOL = 12;

    /**
     * 统计报表-按区域统计-excel
     */
    public static final Integer DISTRICT_STATISTICS_EXCEL = 13;

    /**
     * 统计报表-按学校统计-excel
     */
    public static final Integer SCHOOL_STATISTICS_EXCEL = 14;

    /**
     * 多端管理-学校管理-筛查记录【问卷导出】
     */
    public static final Integer MULTI_TERMINAL_SCHOOL_SCREENING_RECORD_EXCEL = 15;

    /**
     * 统计报表-按区域统计-rec文件
     */
    public static final Integer DISTRICT_STATISTICS_REC = 16;

    /**
     * 统计报表-按学校统计-rec文件
     */
    public static final Integer SCHOOL_STATISTICS_REC = 17;

    /**
     * 多端管理-筛查机构管理-筛查记录【rec文件】
     */
    public static final Integer MULTI_TERMINAL_ORG_SCREENING_RECORD_REC = 18;

}
