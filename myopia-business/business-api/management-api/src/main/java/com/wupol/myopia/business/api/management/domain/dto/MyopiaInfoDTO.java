package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 学生近视情况
 * @Author wulizhou
 * @Date 2022/12/26 12:27
 */
@Data
@Accessors(chain = true)
public class MyopiaInfoDTO {

    /**
     * 近视情况（性别）
     */
    private List<MyopiaDTO> genderMyopia;

    /**
     * 男生占总体近视率
     */
    private Float maleGeneralMyopiaRatio;

    /**
     * 女生占总体近视率
     */
    private Float femaleGeneralMyopiaRatio;

    /**
     * 学生近视监测结果（年级）
     */
    private List<StudentGenderMyopia> gradeMyopia;

    /**
     * 年级近视总结
     */
    private List<SummaryDTO> gradeMyopiaSummary;

    /**
     * 学生近视监测结果（班级）
     */
    private List<StudentGenderMyopia> classMyopia;

    @Data
    @Accessors(chain = true)
    public static class StudentGenderMyopia extends GenderMyopiaInfoDTO {

        /**
         * 年级名称
         */
        private String gradeName;

        /**
         * 班级名称
         */
        private String className;

        /**
         * rowSpan
         */
        private Integer rowSpan;

    }

}
