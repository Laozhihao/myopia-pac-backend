package com.wupol.myopia.business.common.utils.constant;

/**
 * 导出类型常量
 *
 * @author Simple4H
 */
public interface ExportTypeConst {

    /**
     * 计划
     */
    Integer PLAN = 1;

    /**
     * 学校
     */
    Integer SCHOOL = 2;

    /**
     * 年级
     */
    Integer GRADE = 3;

    /**
     * 班级
     */
    Integer CLASS = 4;

    /**
     * 区域
     */
    Integer DISTRICT = 5;

    /**
     * 问卷
     */
    Integer QUESTIONNAIRE = 6;

    // ===============问卷模块=================

    /**
     * 工作台->机构筛查记录【问卷数据】
     */
    Integer SCREENING_RECORD = 10;

    /**
     * 工作台-问卷管理【页面级按钮：下载问卷数据】
     */
    Integer QUESTIONNAIRE_PAGE = 11;
    /**
     * 工作台-问卷管理【学校列表操作：下载问卷数据】
     */
    Integer QUESTIONNAIRE_SCHOOL = 12;

    /**
     * 统计报表-按区域统计
     */
    Integer DISTRICT_STATISTICS = 13;

    /**
     * 统计报表-按学校统计
     */
    Integer SCHOOL_STATISTICS = 14;

    /**
     * 多端管理-学校管理-筛查记录【问卷导出】
     */
    Integer MULTI_TERMINAL_SCHOOL_SCREENING_RECORD = 15;
}
