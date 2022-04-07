package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 复查告知书导出数据
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ReviewInformExportDataDTO {

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 学生名称
     */
    private String studentName;

    /**
     * 身高
     */
    private String height;

    /**
     * 体重
     */
    private String weight;

    /**
     * 复查报告的时间
     */
    private Date screeningDate;

    /**
     * 筛查时间
     */
    private Date planDate;

}
