package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 学生视力情况
 * @Author wulizhou
 * @Date 2022/12/26 12:28
 */
@Data
@Builder
@Accessors(chain = true)
public class VisionInfoDTO {

    /**
     * 整体视力程度情况
     */
    private MyopiaLevelDTO general;

    /**
     * 视力不良程度情况
     */
    private MyopiaLevelDTO lowVision;

    /**
     * 视力不良总结
     */
    private LowVisionSummary lowVisionSummary;

    /**
     * 视力程度情况（性别）
     */
    private List<GenderMyopiaLevel> genderVision;

    /**
     * 性别视力情况总结
     */
    private List<SummaryDTO> genderVisionSummary;

    /**
     * 视力程度情况（年级）
     */
    private List<StudentMyopiaLevel> gradeVision;

    /**
     * 年级视力总结
     */
    private SummaryDTO gradeVisionSummary;

    /**
     * 视力程度情况（班级）
     */
    private List<StudentMyopiaLevel> classVision;

    @Data
    @Accessors(chain = true)
    public static class GenderMyopiaLevel extends MyopiaLevelDTO {

        /**
         * 性别：男/女
         */
        private String gender;

        public static GenderMyopiaLevel getInstance(String gender) {
            GenderMyopiaLevel genderMyopiaLevel = new GenderMyopiaLevel();
            return genderMyopiaLevel.setGender(gender);
        }

    }

    @Data
    @Accessors(chain = true)
    public static class StudentMyopiaLevel extends MyopiaLevelDTO {

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
        public static StudentMyopiaLevel getGradeInstance(String gradeName) {
            StudentMyopiaLevel gradeMyopiaLevel = new StudentMyopiaLevel();
            return gradeMyopiaLevel.setGradeName(gradeName).setRowSpan(1);
        }

        /**
         * 获取以班级为维度的对象
         * @param gradeName
         * @param className
         * @return
         */
        public static StudentMyopiaLevel getClassInstance(String gradeName, String className) {
            StudentMyopiaLevel classMyopiaLevel = new StudentMyopiaLevel();
            return classMyopiaLevel.setGradeName(gradeName).setClassName(className).setRowSpan(0);
        }

    }

    /**
     * 视力不良总结
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LowVisionSummary extends SummaryDTO {

        /**
         * 占视力不良总人数比
         */
        private Float lowVisionRatio;

    }

}
