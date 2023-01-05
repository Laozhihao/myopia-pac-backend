package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.SummaryDTO;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 幼儿园学生视力情况
 * @Author wulizhou
 * @Date 2023/1/4 10:56
 */
@Data
@Builder
@Accessors(chain = true)
public class KindergartenVisionInfoDTO {

    /**
     * 视力程度情况（性别）
     */
    private List<KindergartenGenderLowVision> genderVision;

    /**
     * 视力程度情况（年级）
     */
    private List<KindergartenStudentLowVision> gradeVision;

    /**
     * 年级视力总结
     */
    private SummaryDTO gradeVisionSummary;

    /**
     * 视力程度情况（班级）
     */
    private List<KindergartenStudentLowVision> classVision;


    /**
     * 幼儿园视力低常
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KindergartenLowVision {

        /**
         * 有效筛查人数
         */
        private int validScreeningNum;

        /**
         * 视力低常人数
         */
        private int lowVisionNum;

        /**
         * 视力低常率
         */
        private Float lowVisionRatio;

        public void empty() {
            setValidScreeningNum(0);
            setLowVisionNum(0);
            setLowVisionRatio(0.0f);
        }

        /**
         * 生成视力情况
         * @param validScreeningNum
         * @param lowVisionNum
         */
        public void generateData(int validScreeningNum, int lowVisionNum) {
            setValidScreeningNum(validScreeningNum);
            setLowVisionNum(lowVisionNum);
            setLowVisionRatio(MathUtil.divideFloat(lowVisionNum, validScreeningNum));
        }

    }

    /**
     * 性别视力低常情况
     */
    @Data
    @Accessors(chain = true)
    public static class KindergartenGenderLowVision extends KindergartenLowVision {

        /**
         * 性别：男/女/总体情况
         */
        private String gender;

        public static KindergartenGenderLowVision getInstance(String gender) {
            KindergartenGenderLowVision genderLowVision = new KindergartenGenderLowVision();
            return genderLowVision.setGender(gender);
        }

    }

    @Data
    @Accessors(chain = true)
    public static class KindergartenStudentLowVision extends KindergartenLowVision {

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
        public static KindergartenStudentLowVision getGradeInstance(String gradeName) {
            KindergartenStudentLowVision gradeLowVision = new KindergartenStudentLowVision();
            return gradeLowVision.setGradeName(gradeName).setRowSpan(1);
        }

        /**
         * 获取以班级为维度的对象
         * @param gradeName
         * @param className
         * @return
         */
        public static KindergartenStudentLowVision getClassInstance(String gradeName, String className) {
            KindergartenStudentLowVision classLowVision = new KindergartenStudentLowVision();
            return classLowVision.setGradeName(gradeName).setClassName(className).setRowSpan(0);
        }

    }

}
