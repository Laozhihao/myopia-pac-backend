package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.MapUtils;
import com.wupol.myopia.base.util.RowSpanUtils;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 屈光情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class KindergartenRefractiveSituationDTO {

    /**
     * 不同性别屈光情况
     */
    private GenderRefractiveSituation genderRefractiveSituation;

    /**
     * 不同年级屈光情况
     */
    private GradeRefractiveSituation gradeRefractiveSituation;

    /**
     * 不同班级屈光情况/欠矫
     */
    private ClassRefractiveSituation classRefractiveSituation;


    /**
     * 不同性别屈光情况
     */
    @Getter
    @Setter
    public static class GenderRefractiveSituation {

        private List<RefractiveSituationItem> items;

        public static GenderRefractiveSituation getInstance(List<StatConclusion> statConclusions) {
            GenderRefractiveSituation genderRefractiveSituation = new GenderRefractiveSituation();
            Map<Integer, List<StatConclusion>> genderStatConclusion = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
            List<RefractiveSituationItem> genderList = GenderEnum.genderList().stream().map(s -> {
                List<StatConclusion> genderStatConclusionList = genderStatConclusion.getOrDefault(s.type, new ArrayList<>());
                RefractiveSituationItem refractiveSituation = new RefractiveSituationItem();
                refractiveSituation.setGenderName(s.desc);
                getRefractiveSituation(genderStatConclusionList, refractiveSituation);
                return refractiveSituation;
            }).collect(Collectors.toList());
            RefractiveSituationItem totalRefractiveSituationItem = new RefractiveSituationItem();
            totalRefractiveSituationItem.setGenderName(CommonConst.TOTAL_DESC);
            genderList.add(getRefractiveSituation(statConclusions, totalRefractiveSituationItem));
            genderRefractiveSituation.setItems(genderList);
            return genderRefractiveSituation;
        }

    }

    /**
     * 不同性别屈光情况
     */
    @Getter
    @Setter
    public static class RefractiveSituationItem extends RefractiveSituation {

        /**
         * 性别名称
         */
        private String genderName;

    }

    /**
     * 不同年级屈光情况
     */
    @Getter
    @Setter
    public static class GradeRefractiveSituation {

        /**
         * 详情
         */
        private List<GradeRefractiveSituationItem> items;

        /**
         * 总结
         */
        private List<RefractiveSituationSummary> summary;

        public static GradeRefractiveSituation getInstance(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap) {
            GradeRefractiveSituation gradeRefractiveSituation = new GradeRefractiveSituation();
            gradeRefractiveSituation.setItems(gradeCodes.stream().map(s -> {
                List<StatConclusion> gradeStatConclusion = statConclusionGradeMap.getOrDefault(s, new ArrayList<>());
                GradeRefractiveSituationItem gradeRefractiveSituationItem = new GradeRefractiveSituationItem();
                gradeRefractiveSituationItem.setGradeName(GradeCodeEnum.getDesc(s));
                return getRefractiveSituation(gradeStatConclusion, gradeRefractiveSituationItem);
            }).collect(Collectors.toList()));
            RefractiveSituationSummary refractiveError = getGradeRefractiveSituationSummary(gradeRefractiveSituation.getItems(), RefractiveSituation::getRefractiveErrorRatio, "refractiveError");
            RefractiveSituationSummary anisometropia = getGradeRefractiveSituationSummary(gradeRefractiveSituation.getItems(), RefractiveSituation::getAnisometropiaRatio, "anisometropia");
            RefractiveSituationSummary insufficientHyperopia = getGradeRefractiveSituationSummary(gradeRefractiveSituation.getItems(), RefractiveSituation::getInsufficientHyperopiaRatio, "insufficientHyperopia");
            gradeRefractiveSituation.setSummary(Lists.newArrayList(refractiveError, anisometropia, insufficientHyperopia));
            return gradeRefractiveSituation;
        }

        /**
         * 总结
         *
         * @return RefractiveSituationDTO.GradeRefractiveSituationSummary
         */
        private static RefractiveSituationSummary getGradeRefractiveSituationSummary(List<GradeRefractiveSituationItem> gradeRefractiveSituationItems, Function<GradeRefractiveSituationItem, Float> myopiaLevelFunction, String keyName) {
            RefractiveSituationSummary refractiveSituationSummary = new RefractiveSituationSummary();
            Map<Float, List<GradeRefractiveSituationItem>> sortMap = MapUtils.sortMap(gradeRefractiveSituationItems.stream().collect(Collectors.groupingBy(myopiaLevelFunction)));
            Float firstKey = MapUtils.getFirstKey(sortMap);
            Map.Entry<Float, List<GradeRefractiveSituationItem>> tail = MapUtils.getLastEntry(sortMap);
            refractiveSituationSummary.setHighName(sortMap.get(tail.getKey()).stream().map(GradeRefractiveSituationItem::getGradeName).collect(Collectors.toList()));
            refractiveSituationSummary.setHighRadio(tail.getKey());
            refractiveSituationSummary.setLowName(sortMap.get(firstKey).stream().map(GradeRefractiveSituationItem::getGradeName).collect(Collectors.toList()));
            refractiveSituationSummary.setLowRadio(firstKey);
            refractiveSituationSummary.setKeyName(keyName);
            return refractiveSituationSummary;
        }

    }

    /**
     * 不同年级屈光情况
     */
    @Getter
    @Setter
    public static class GradeRefractiveSituationItem extends RefractiveSituation {

        /**
         * 年级名称
         */
        private String gradeName;
    }

    /**
     * 不同班级屈光情况
     */
    @Getter
    @Setter
    public static class ClassRefractiveSituation {

        /**
         * 详情
         */
        private List<ClassRefractiveSituationItem> items;

        public static ClassRefractiveSituation getInstance(List<String> gradeCodes, Map<String, List<SchoolClass>> classMap, Map<String, List<StatConclusion>> statConclusionClassMap) {
            ClassRefractiveSituation classRefractiveSituation = new ClassRefractiveSituation();
            List<ClassRefractiveSituationItem> items = new ArrayList<>();
            gradeCodes.forEach(s -> {
                List<SchoolClass> schoolClasses = classMap.get(s);
                AtomicBoolean isFirst = new AtomicBoolean(true);
                schoolClasses.forEach(schoolClass -> {
                    List<StatConclusion> classStatConclusion = statConclusionClassMap.getOrDefault(s + schoolClass.getName(), new ArrayList<>());
                    ClassRefractiveSituationItem classRefractiveSituationItem = new ClassRefractiveSituationItem();
                    classRefractiveSituationItem.setGradeName(GradeCodeEnum.getDesc(s));
                    classRefractiveSituationItem.setClassName(schoolClass.getName());
                    classRefractiveSituationItem.setRowSpan(isFirst, schoolClasses.size());
                    items.add(getRefractiveSituation(classStatConclusion, classRefractiveSituationItem));
                });
            });
            classRefractiveSituation.setItems(items);
            return classRefractiveSituation;
        }
    }

    /**
     * 不同班级屈光情况
     */
    @Getter
    @Setter
    public static class ClassRefractiveSituationItem extends RefractiveSituation {

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
            rowSpan = RowSpanUtils.setRowSpan(isFirst, size);
        }
    }

    /**
     * 总结
     */
    @Getter
    @Setter
    public static class RefractiveSituationSummary {

        /**
         * 名称
         */
        private String keyName;

        /**
         * 最高名称
         */
        private List<String> highName;

        /**
         * 最高百分比
         */
        private Float highRadio;

        /**
         * 最低名称
         */
        private List<String> lowName;

        /**
         * 最低百分比
         */
        private Float lowRadio;
    }

    /**
     * 屈光情况
     */
    @Getter
    @Setter
    public static class RefractiveSituation {

        /**
         * 筛查学生
         */
        private Long screeningStudentNum;

        /**
         * 屈光不正人数
         */
        private Long refractiveErrorNum;

        /**
         * 屈光不正率
         */
        private Float refractiveErrorRatio;

        /**
         * 屈光参差人数
         */
        private Long anisometropiaNum;

        /**
         * 屈光参差率
         */
        private Float anisometropiaRatio;

        /**
         * 远视储备不足人数
         */
        private Long insufficientHyperopiaNum;

        /**
         * 远视储备不足率
         */
        private Float insufficientHyperopiaRatio;

    }

    /**
     * 屈光情况
     *
     * @return T
     */
    private static <T extends RefractiveSituation> T getRefractiveSituation(List<StatConclusion> statConclusions, T t) {
        long screeningTotal = statConclusions.size();
        t.setScreeningStudentNum(screeningTotal);
        t.setRefractiveErrorNum(statConclusions.stream().filter(s -> Objects.equals(s.getIsRefractiveError(), Boolean.TRUE)).count());
        t.setRefractiveErrorRatio(BigDecimalUtil.divideRadio(t.getRefractiveErrorNum(), screeningTotal));
        t.setAnisometropiaNum(statConclusions.stream().filter(s -> Objects.equals(s.getIsAnisometropia(), Boolean.TRUE)).count());
        t.setAnisometropiaRatio(BigDecimalUtil.divideRadio(t.getAnisometropiaNum(), screeningTotal));
        t.setInsufficientHyperopiaNum(statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.ZERO_SP.getCode())).count());
        t.setInsufficientHyperopiaRatio(BigDecimalUtil.divideRadio(t.getInsufficientHyperopiaNum(), screeningTotal));
        return t;
    }

}
