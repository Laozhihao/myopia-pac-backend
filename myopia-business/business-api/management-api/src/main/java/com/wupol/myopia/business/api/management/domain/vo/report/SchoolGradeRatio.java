package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 学校年级占比
 *
 * @author hang.yuan
 * @date 2022/6/8
 */
@Data
public class SchoolGradeRatio {
    /**
     * 最高占比年级
     */
    private String maxGrade;
    /**
     * 最低占比年级
     */
    private String minGrade;
    /**
     * 最高占比
     */
    private String maxRatio;
    /**
     * 最低占比
     */
    private String minRatio;

}