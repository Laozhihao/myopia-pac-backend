package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import com.wupol.myopia.business.api.management.domain.dto.MyopiaDTO;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 学生近视情况
 * @Author wulizhou
 * @Date 2022/12/26 12:27
 */
@Data
@Builder
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
    public static class StudentGenderMyopia extends GenderMyopiaInfoDTO implements HasDimension {

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

        /**
         * 获取以年级为维度的对象
         * @param gradeName
         * @return
         */
        public static StudentGenderMyopia getGradeInstance(String gradeName) {
            StudentGenderMyopia studentGenderMyopia = new StudentGenderMyopia();
            return studentGenderMyopia.setGradeName(gradeName).setRowSpan(1);
        }

        /**
         * 获取以班级为维度的对象
         * @param gradeName
         * @param className
         * @return
         */
        public static StudentGenderMyopia getClassInstance(String gradeName, String className) {
            StudentGenderMyopia studentGenderMyopia = new StudentGenderMyopia();
            return studentGenderMyopia.setGradeName(gradeName).setClassName(className).setRowSpan(0);
        }

        @Override
        public String dimensionName() {
            return getGradeName();
        }
    }

}
