package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.MapUtils;
import com.wupol.myopia.base.util.RowSpanUtils;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
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
public class RefractiveSituationDTO {

    /**
     * 信息
     */
    private RefractiveSituationInfo refractiveSituationInfo;

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
     * 屈光情况信息
     */
    @Getter
    @Setter
    public static class RefractiveSituationInfo extends RefractiveSituation {

        /**
         * 屈光不正人数
         */
        private Long refractiveErrorNum;

        public static RefractiveSituationInfo getInstance(List<StatConclusion> statConclusions) {
            RefractiveSituationInfo refractiveSituationInfo = getRefractiveSituation(statConclusions, new RefractiveSituationInfo());
            refractiveSituationInfo.setRefractiveErrorNum(statConclusions.stream().filter(s -> Objects.equals(s.getIsRefractiveError(), Boolean.TRUE)).count());
            return refractiveSituationInfo;
        }
    }

    /**
     * 不同性别屈光情况
     */
    @Getter
    @Setter
    public static class GenderRefractiveSituation {

        private List<RefractiveSituationItem> items;

        public static GenderRefractiveSituation getInstance(List<StatConclusion> statConclusions) {
            RefractiveSituationDTO.GenderRefractiveSituation genderRefractiveSituation = new RefractiveSituationDTO.GenderRefractiveSituation();
            Map<Integer, List<StatConclusion>> genderStatConclusion = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
            List<RefractiveSituationDTO.RefractiveSituationItem> genderList = GenderEnum.genderList().stream().map(s -> {
                List<StatConclusion> genderStatConclusionList = genderStatConclusion.getOrDefault(s.type, new ArrayList<>());
                RefractiveSituationDTO.RefractiveSituationItem refractiveSituation = new RefractiveSituationDTO.RefractiveSituationItem();
                refractiveSituation.setGenderName(s.desc);
                getRefractiveSituation(genderStatConclusionList, refractiveSituation);
                return refractiveSituation;
            }).collect(Collectors.toList());
            RefractiveSituationDTO.RefractiveSituationItem totalRefractiveSituationItem = new RefractiveSituationDTO.RefractiveSituationItem();
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
        private List<CompareSummaryDTO> summary;

        public static GradeRefractiveSituation getInstance(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap) {
            RefractiveSituationDTO.GradeRefractiveSituation gradeRefractiveSituation = new RefractiveSituationDTO.GradeRefractiveSituation();
            gradeRefractiveSituation.setItems(gradeCodes.stream().map(s -> {
                List<StatConclusion> gradeStatConclusion = statConclusionGradeMap.getOrDefault(s, new ArrayList<>());
                RefractiveSituationDTO.GradeRefractiveSituationItem gradeRefractiveSituationItem = new RefractiveSituationDTO.GradeRefractiveSituationItem();
                gradeRefractiveSituationItem.setGradeName(GradeCodeEnum.getDesc(s));
                return getRefractiveSituation(gradeStatConclusion, gradeRefractiveSituationItem);
            }).collect(Collectors.toList()));
            CompareSummaryDTO lowMyopiaSummary = getGradeRefractiveSituationSummary(gradeRefractiveSituation.getItems(), RefractiveSituationDTO.RefractiveSituation::getLowMyopiaRatio, "lowMyopia");
            CompareSummaryDTO highMyopiaSummary = getGradeRefractiveSituationSummary(gradeRefractiveSituation.getItems(), RefractiveSituationDTO.RefractiveSituation::getHighMyopiaRatio, "highMyopia");
            CompareSummaryDTO astigmatismSummary = getGradeRefractiveSituationSummary(gradeRefractiveSituation.getItems(), RefractiveSituationDTO.RefractiveSituation::getAstigmatismRatio, "astigmatism");
            gradeRefractiveSituation.setSummary(Lists.newArrayList(lowMyopiaSummary, highMyopiaSummary, astigmatismSummary));
            return gradeRefractiveSituation;
        }

        /**
         * 总结
         *
         * @return CompareSummaryDTO
         */
        private static CompareSummaryDTO getGradeRefractiveSituationSummary(List<RefractiveSituationDTO.GradeRefractiveSituationItem> gradeRefractiveSituationItems, Function<GradeRefractiveSituationItem, Float> myopiaLevelFunction, String keyName) {
            CompareSummaryDTO refractiveSituationSummary = new CompareSummaryDTO();
            Map<Float, List<RefractiveSituationDTO.GradeRefractiveSituationItem>> sortMap = MapUtils.sortMap(gradeRefractiveSituationItems.stream().collect(Collectors.groupingBy(myopiaLevelFunction)));
            Float firstKey = MapUtils.getFirstKey(sortMap);
            Map.Entry<Float, List<RefractiveSituationDTO.GradeRefractiveSituationItem>> tail = MapUtils.getLastEntry(sortMap);
            refractiveSituationSummary.setHighName(sortMap.get(tail.getKey()).stream().map(RefractiveSituationDTO.GradeRefractiveSituationItem::getGradeName).collect(Collectors.toList()));
            refractiveSituationSummary.setHighRadio(tail.getKey());
            refractiveSituationSummary.setLowName(sortMap.get(firstKey).stream().map(RefractiveSituationDTO.GradeRefractiveSituationItem::getGradeName).collect(Collectors.toList()));
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
            RefractiveSituationDTO.ClassRefractiveSituation classRefractiveSituation = new RefractiveSituationDTO.ClassRefractiveSituation();
            List<RefractiveSituationDTO.ClassRefractiveSituationItem> items = new ArrayList<>();
            gradeCodes.forEach(s -> {
                List<SchoolClass> schoolClasses = classMap.get(s);
                AtomicBoolean isFirst = new AtomicBoolean(true);
                schoolClasses.forEach(schoolClass -> {
                    List<StatConclusion> classStatConclusion = statConclusionClassMap.getOrDefault(s + schoolClass.getName(), new ArrayList<>());
                    RefractiveSituationDTO.ClassRefractiveSituationItem classRefractiveSituationItem = new RefractiveSituationDTO.ClassRefractiveSituationItem();
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
         * 低度近视人数
         */
        private Long lowMyopiaNum;

        /**
         * 低度近视百分比
         */
        private Float lowMyopiaRatio;

        /**
         * 高度近视人数
         */
        private Long highMyopiaNum;

        /**
         * 高度近视百分比
         */
        private Float highMyopiaRatio;

        /**
         * 散光人数
         */
        private Long astigmatismNum;

        /**
         * 散光百分比
         */
        private Float astigmatismRatio;
    }

    /**
     * 屈光情况
     *
     * @return T
     */
    private static <T extends RefractiveSituationDTO.RefractiveSituation> T getRefractiveSituation(List<StatConclusion> statConclusions, T t) {
        long screeningTotal = statConclusions.size();
        t.setScreeningStudentNum(screeningTotal);
        t.setLowMyopiaNum(myopiaLevelCount(statConclusions, MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code));
        t.setLowMyopiaRatio(BigDecimalUtil.divideRadio(t.getLowMyopiaNum(), screeningTotal));
        t.setHighMyopiaNum(myopiaLevelCount(statConclusions, MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code));
        t.setHighMyopiaRatio(BigDecimalUtil.divideRadio(t.getHighMyopiaNum(), screeningTotal));
        t.setAstigmatismNum(statConclusions.stream().filter(s -> Objects.equals(s.getIsAstigmatism(), Boolean.TRUE)).count());
        t.setAstigmatismRatio(BigDecimalUtil.divideRadio(t.getAstigmatismNum(), screeningTotal));
        return t;
    }

    /**
     * 近视
     *
     * @return Long
     */
    private static Long myopiaLevelCount(List<StatConclusion> statConclusions, Integer type) {
        return statConclusions.stream().filter(s -> Objects.equals(s.getMyopiaLevel(), type)).count();
    }

}
