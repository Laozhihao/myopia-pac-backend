package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.common.utils.constant.VisionCorrection;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 视力矫正情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class VisionCorrectionSituationDTO {

    /**
     * 信息
     */
    private VisionCorrectionSituationInfo visionCorrectionSituationInfo;

    /**
     * 戴镜情况
     */
    private WearingGlasses wearingGlasses;

    /**
     * 矫正情况
     */
    private CorrectionSituation correctionSituation;

    /**
     * 年级未矫/欠矫
     */
    private GradeUnderCorrectedAndUncorrected gradeUnderCorrectedAndUncorrected;

    /**
     * 班级未矫/欠矫
     */
    private ClassUnderCorrectedAndUncorrected classUnderCorrectedAndUncorrected;

    /**
     * 视力矫正情况
     */
    @Getter
    @Setter
    public static class VisionCorrectionSituationInfo {

        /**
         * 筛查学生
         */
        private Long screeningStudentNum;

        /**
         * 戴镜人数
         */
        private Long wearingGlassesNum;

        /**
         * 戴镜人数百分比
         */
        private Float wearingGlassesRatio;

        public static VisionCorrectionSituationInfo getInstance(List<StatConclusion> statConclusions) {
            VisionCorrectionSituationInfo info = new VisionCorrectionSituationInfo();
            long screeningTotal = statConclusions.size();
            info.setScreeningStudentNum(screeningTotal);
            info.setWearingGlassesNum(statConclusions.stream().filter(s -> !Objects.equals(s.getGlassesType(), GlassesTypeEnum.NOT_WEARING.getCode())).count());
            info.setWearingGlassesRatio(BigDecimalUtil.divideRadio(info.getWearingGlassesNum(), info.getScreeningStudentNum()));
            return info;
        }
    }

    /**
     * 戴镜情况
     */
    @Getter
    @Setter
    public static class WearingGlasses {

        /**
         * 戴镜详情
         */
        private WearingGlassesItem wearingGlassesItem;

        /**
         * 表格
         */
        private List<PieChart> table;

        public static WearingGlasses getInstance(List<StatConclusion> statConclusions) {
            long screeningTotal = statConclusions.size();
            VisionCorrectionSituationDTO.WearingGlasses wearingGlasses = new VisionCorrectionSituationDTO.WearingGlasses();
            VisionCorrectionSituationDTO.WearingGlassesItem wearingGlassesItem = new VisionCorrectionSituationDTO.WearingGlassesItem();
            wearingGlassesItem.setScreeningStudentNum(screeningTotal);
            wearingGlassesItem.setNotWearingNum(wearingGlassesCount(statConclusions, GlassesTypeEnum.NOT_WEARING.getCode()));
            wearingGlassesItem.setNotWearingRatio(BigDecimalUtil.divideRadio(wearingGlassesItem.getNotWearingNum(), screeningTotal));
            wearingGlassesItem.setFrameGlassesNum(wearingGlassesCount(statConclusions, GlassesTypeEnum.FRAME_GLASSES.getCode()));
            wearingGlassesItem.setFrameGlassesRatio(BigDecimalUtil.divideRadio(wearingGlassesItem.getFrameGlassesNum(), screeningTotal));
            wearingGlassesItem.setContactLensNum(wearingGlassesCount(statConclusions, GlassesTypeEnum.CONTACT_LENS.getCode()));
            wearingGlassesItem.setContactLensRatio(BigDecimalUtil.divideRadio(wearingGlassesItem.getContactLensNum(), screeningTotal));
            wearingGlassesItem.setOrthokeratologyNum(wearingGlassesCount(statConclusions, GlassesTypeEnum.ORTHOKERATOLOGY.getCode()));
            wearingGlassesItem.setOrthokeratologyRatio(BigDecimalUtil.divideRadio(wearingGlassesItem.getOrthokeratologyNum(), screeningTotal));
            wearingGlasses.setWearingGlassesItem(wearingGlassesItem);
            wearingGlasses.setTable(null);
            return wearingGlasses;
        }

        /**
         * 戴镜统计
         *
         * @return Long
         */
        private static Long wearingGlassesCount(List<StatConclusion> statConclusions, Integer glassesType) {
            return statConclusions.stream().filter(s -> Objects.equals(s.getGlassesType(), glassesType)).count();
        }
    }

    /**
     * 戴镜详情
     */
    @Getter
    @Setter
    public static class WearingGlassesItem {

        /**
         * 筛查学生
         */
        private Long screeningStudentNum;

        /**
         * 不戴镜人数
         */
        private Long notWearingNum;

        /**
         * 不戴镜百分比
         */
        private Float notWearingRatio;

        /**
         * 框架眼镜人数
         */
        private Long frameGlassesNum;

        /**
         * 框架眼镜百分比
         */
        private Float frameGlassesRatio;

        /**
         * 隐形眼镜人数
         */
        private Long contactLensNum;

        /**
         * 隐形眼镜人数百分比
         */
        private Float contactLensRatio;

        /**
         * 夜戴角膜塑形镜人数
         */
        private Long orthokeratologyNum;

        /**
         * 夜戴角膜塑形镜人数百分比
         */
        private Float orthokeratologyRatio;
    }

    /**
     * 矫正情况
     */
    @Getter
    @Setter
    public static class CorrectionSituation {

        /**
         * 矫正情况
         */
        private UnderCorrectedAndUncorrected underCorrectedAndUncorrected;

        /**
         * 表格
         */
        private List<PieChart> table;

        public static CorrectionSituation getInstance(List<StatConclusion> statConclusions) {
            VisionCorrectionSituationDTO.CorrectionSituation correctionSituation = new VisionCorrectionSituationDTO.CorrectionSituation();
            correctionSituation.setUnderCorrectedAndUncorrected(getUnderCorrectedAndUncorrectedInfo(statConclusions, new VisionCorrectionSituationDTO.UnderCorrectedAndUncorrected()));
            correctionSituation.setTable(null);
            return correctionSituation;
        }
    }

    /**
     * 年级矫正情况
     */
    @Getter
    @Setter
    public static class GradeUnderCorrectedAndUncorrected {

        /**
         * 详情
         */
        private List<GradeUnderCorrectedAndUncorrectedItem> items;

        /**
         * 表格
         */
        private Object table;

        public static GradeUnderCorrectedAndUncorrected getInstance(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap) {
            VisionCorrectionSituationDTO.GradeUnderCorrectedAndUncorrected gradeUnderCorrectedAndUncorrected = new VisionCorrectionSituationDTO.GradeUnderCorrectedAndUncorrected();
            List<VisionCorrectionSituationDTO.GradeUnderCorrectedAndUncorrectedItem> gradeUnderCorrectedAndUncorrectedItems = gradeCodes.stream().map(s -> {
                List<StatConclusion> gradeStatConclusion = statConclusionGradeMap.getOrDefault(s, new ArrayList<>());
                VisionCorrectionSituationDTO.GradeUnderCorrectedAndUncorrectedItem correctedAndUncorrectedItem = new VisionCorrectionSituationDTO.GradeUnderCorrectedAndUncorrectedItem();
                correctedAndUncorrectedItem.setGradeName(GradeCodeEnum.getDesc(s));
                return getUnderCorrectedAndUncorrectedInfo(gradeStatConclusion, correctedAndUncorrectedItem);
            }).collect(Collectors.toList());
            gradeUnderCorrectedAndUncorrected.setItems(gradeUnderCorrectedAndUncorrectedItems);
            gradeUnderCorrectedAndUncorrected.setTable(null);
            return gradeUnderCorrectedAndUncorrected;
        }
    }

    /**
     * 年级矫正情况
     */
    @Getter
    @Setter
    public static class GradeUnderCorrectedAndUncorrectedItem extends UnderCorrectedAndUncorrected {

        /**
         * 年级名称
         */
        private String gradeName;
    }

    /**
     * 班级矫正情况
     */
    @Getter
    @Setter
    public static class ClassUnderCorrectedAndUncorrected {

        /**
         * 详情
         */
        private List<ClassUnderCorrectedAndUncorrectedItem> items;

        public static ClassUnderCorrectedAndUncorrected getInstance(List<String> gradeCodes, Map<String, List<SchoolClass>> classMap, Map<String, List<StatConclusion>> statConclusionClassMap) {
            VisionCorrectionSituationDTO.ClassUnderCorrectedAndUncorrected classUnderCorrectedAndUncorrected = new VisionCorrectionSituationDTO.ClassUnderCorrectedAndUncorrected();
            List<VisionCorrectionSituationDTO.ClassUnderCorrectedAndUncorrectedItem> items = new ArrayList<>();
            gradeCodes.forEach(s -> {
                List<SchoolClass> schoolClasses = classMap.get(s);
                AtomicBoolean isFirst = new AtomicBoolean(true);
                schoolClasses.forEach(schoolClass -> {
                    List<StatConclusion> classStatConclusion = statConclusionClassMap.getOrDefault(s + schoolClass.getName(), new ArrayList<>());
                    VisionCorrectionSituationDTO.ClassUnderCorrectedAndUncorrectedItem classUnderCorrectedAndUncorrectedItem = new VisionCorrectionSituationDTO.ClassUnderCorrectedAndUncorrectedItem();
                    classUnderCorrectedAndUncorrectedItem.setGradeName(GradeCodeEnum.getDesc(s));
                    classUnderCorrectedAndUncorrectedItem.setClassName(schoolClass.getName());
                    classUnderCorrectedAndUncorrectedItem.setRowSpan(isFirst, schoolClasses.size());
                    items.add(getUnderCorrectedAndUncorrectedInfo(classStatConclusion, classUnderCorrectedAndUncorrectedItem));
                });
            });
            classUnderCorrectedAndUncorrected.setItems(items);
            return classUnderCorrectedAndUncorrected;
        }
    }

    /**
     * 班级矫正情况
     */
    @Getter
    @Setter
    public static class ClassUnderCorrectedAndUncorrectedItem extends UnderCorrectedAndUncorrected {

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

        public void setRowSpan(AtomicBoolean isFirst, Integer size) {
            if (isFirst.get()) {
                isFirst.set(false);
                rowSpan = size;
            } else {
                rowSpan = 0;
            }
        }
    }

    /**
     * 矫正情况
     */
    @Getter
    @Setter
    public static class UnderCorrectedAndUncorrected {
        /**
         * 筛查学生
         */
        private Long screeningStudentNum;

        /**
         * 未矫人数
         */
        private Long uncorrectedNum;

        /**
         * 未矫百分比
         */
        private Float uncorrectedRatio;

        /**
         * 欠矫人数
         */
        private Long underCorrectedNum;

        /**
         * 欠矫百分比
         */
        private Float underCorrectedRatio;
    }

    /**
     * 矫正统计
     *
     * @return T
     */
    private static <T extends VisionCorrectionSituationDTO.UnderCorrectedAndUncorrected> T getUnderCorrectedAndUncorrectedInfo(List<StatConclusion> statConclusions, T t) {
        t.setScreeningStudentNum((long) statConclusions.size());
        t.setUncorrectedNum(underCorrectedAndUncorrectedCount(statConclusions, VisionCorrection.UNCORRECTED.getCode()));
        t.setUncorrectedRatio(BigDecimalUtil.divideRadio(t.getUncorrectedNum(), statConclusions.stream().filter(s -> Objects.equals(s.getIsMyopia(), Boolean.TRUE)).count()));
        t.setUnderCorrectedNum(underCorrectedAndUncorrectedCount(statConclusions, VisionCorrection.UNDER_CORRECTED.getCode()));
        t.setUnderCorrectedRatio(BigDecimalUtil.divideRadio(t.getUnderCorrectedNum(), statConclusions.stream().filter(s -> !Objects.equals(s.getGlassesType(), GlassesTypeEnum.NOT_WEARING.getCode())).count()));
        return t;
    }

    /**
     * 矫正统计
     *
     * @return Long
     */
    private static Long underCorrectedAndUncorrectedCount(List<StatConclusion> statConclusions, Integer type) {
        return statConclusions.stream().filter(s -> Objects.equals(s.getVisionCorrection(), type)).count();
    }
}