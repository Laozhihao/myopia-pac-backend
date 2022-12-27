package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Builder;
import lombok.Data;
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
    public static class GenderMyopiaLevel extends MyopiaLevelDTO {

        /**
         * 性别：男/女
         */
        private String gender;

    }

    @Data
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

    }

}
