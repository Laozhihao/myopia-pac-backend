package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 报告工具
 *
 * @author hang.yuan 2022/6/1 09:45
 */
@UtilityClass
public class ReportUtil {


    /**
     * 获取map中Value最大值及对应的Key
     */
    public static <T, K, V> TwoTuple<K, V> getMaxMap(Map<K, T> map, Function<T, Integer> function, Function<T, V> mapper) {
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries, ((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0) - Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(), Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
    }

    /**
     * 获取map中Value最小值及对应的Key
     */
    public static <T, K, V> TwoTuple<K, V> getMinMap(Map<K, T> map, Function<T, Integer> function, Function<T, V> mapper) {
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries, Comparator.comparingInt(o -> Optional.ofNullable(o.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(), Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
    }

    public static Integer getLessAge(Integer age) {
        if (age < 6) {
            return 6;
        } else if (age < 7) {
            return 7;
        } else if (age < 8) {
            return 8;
        } else if (age < 9) {
            return 9;
        } else if (age < 10) {
            return 10;
        } else if (age < 11) {
            return 11;
        } else if (age < 12) {
            return 12;
        } else if (age < 13) {
            return 13;
        } else if (age < 14) {
            return 14;
        } else if (age < 15) {
            return 15;
        } else if (age < 16) {
            return 16;
        } else if (age < 17) {
            return 17;
        } else if (age < 18) {
            return 18;
        } else {
            return 19;
        }
    }

    public static List<Integer> dynamicAgeSegment(List<StatConclusion> statConclusionList) {
        List<Integer> ageSegmentList = Lists.newArrayList();
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return ageSegmentList;
        }
        Integer min = statConclusionList.stream().map(StatConclusion::getAge).min(Comparator.comparing(Integer::intValue)).orElse(null);
        Integer max = statConclusionList.stream().map(StatConclusion::getAge).max(Comparator.comparing(Integer::intValue)).orElse(null);
        if (max >= 18) {
            max = 18;
        }
        if (min >= 18) {
            min = 18;
        }
        if (max <= 5){
            max = 6;
        }
        if (min <= 5){
            min = 6;
        }
        if (Objects.equals(min, max)) {
            ageSegmentList.add(min + 1);
            return ageSegmentList;
        }
        for (int i = min; i <= max + 1; i++) {
            ageSegmentList.add(i);
        }
        return ageSegmentList;
    }

    public static <T> AgeRatioVO getAgeRatio(Map<Integer, T> saprodontiaNumMap, Function<T, Integer> function, Function<T, String> mapper) {
        TwoTuple<Integer, String> maxTuple = ReportUtil.getMaxMap(saprodontiaNumMap, function, mapper);
        TwoTuple<Integer, String> minTuple = ReportUtil.getMinMap(saprodontiaNumMap, function, mapper);
        AgeRatioVO ageRatio = new AgeRatioVO();
        ageRatio.setMaxName(AgeSegmentEnum.get(maxTuple.getFirst()).getDesc());
        ageRatio.setMinName(AgeSegmentEnum.get(minTuple.getFirst()).getDesc());
        ageRatio.setMaxProportion(maxTuple.getSecond());
        ageRatio.setMinProportion(minTuple.getSecond());
        return ageRatio;
    }

    public <T extends Num> SexCompare getRatioCompare(List<T> sexList, Function<T, BigDecimal> function, Function<T, String> mapper) {
        if (CollectionUtil.isEmpty(sexList)) {
            return null;
        }
        CollectionUtil.sort(sexList, Comparator.comparing(function).reversed());
        SexCompare sex = new SexCompare();
        if (sexList.size() == 1) {
            T num = sexList.get(0);
            if (Objects.equals(GenderEnum.MALE.type, num.getGender())) {
                setSexCompare(num, null, mapper, sex, GenderEnum.FEMALE.desc, ReportConst.ZERO_RATIO_STR);
            } else {
                setSexCompare(num, null, mapper, sex, GenderEnum.MALE.desc, ReportConst.ZERO_RATIO_STR);
            }
        }
        if (sexList.size() == 2) {
            T forward = sexList.get(0);
            T back = sexList.get(1);
            setSexCompare(forward, back, mapper, sex, null, null);
        }
        return sex;
    }

    private <T extends Num> void setSexCompare(T forward, T back, Function<T, String> mapper,
                                               SexCompare sex,
                                               String backSex, String zeroRatio) {

        String forwardRatio = mapper.apply(forward);
        sex.setForwardSex(GenderEnum.getName(forward.getGender())+ReportConst.SEX);
        sex.setForwardRatio(forwardRatio);

        if (Objects.nonNull(back)) {
            String backRatio = mapper.apply(back);
            sex.setBackSex(GenderEnum.getName(back.getGender())+ReportConst.SEX);
            sex.setBackRatio(backRatio);
            setSymbol(sex, forwardRatio, backRatio);
        } else {
            sex.setBackSex(backSex+ReportConst.SEX);
            sex.setBackRatio(zeroRatio);
            setSymbol(sex, forwardRatio, zeroRatio);
        }
    }

    private void setSymbol(SexCompare sex, String forward, String back) {
        if (Objects.equals(forward, back)) {
            sex.setSymbol("=");
        } else {
            sex.setSymbol(">");
        }
    }

    public static BigDecimal getRatioNotSymbol(String ratio) {
        return new BigDecimal(ratio.substring(0, ratio.length() - 1));
    }


    public static <T> GradeRatio getGradeRatio(Map<String, T> numMap, Function<T, Integer> function, Function<T, String> mapper) {
        if (CollectionUtil.isNotEmpty(numMap)) {
            return null;
        }
        GradeRatio gradeRatio = new GradeRatio();
        TwoTuple<String, String> tuple = ReportUtil.getMaxMap(numMap, function, mapper);
        gradeRatio.setGrade(tuple.getFirst());
        gradeRatio.setRatio(tuple.getSecond());
        return gradeRatio;
    }

    public static <T> SchoolGradeRatio getSchoolGradeRatio(Map<String, T> numMap, Function<T, Integer> function, Function<T, String> mapper) {
        if (CollectionUtil.isEmpty(numMap)) {
            return null;
        }
        SchoolGradeRatio gradeRatio = new SchoolGradeRatio();
        TwoTuple<String, String> maxTuple = ReportUtil.getMaxMap(numMap, function, mapper);
        TwoTuple<String, String> minTuple = ReportUtil.getMinMap(numMap, function, mapper);
        gradeRatio.setMaxGrade(maxTuple.getFirst());
        gradeRatio.setMaxRatio(maxTuple.getSecond());
        gradeRatio.setMinGrade(minTuple.getFirst());
        gradeRatio.setMinRatio(minTuple.getSecond());
        return gradeRatio;
    }


    public boolean getSchoolGrade(List<StatConclusion> statConclusionList) {
        Map<String, List<StatConclusion>> collect = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        return collect.size() <= 1;
    }

    public static <K> void getSaprodontiaNum(K key, List<StatConclusion> statConclusionList, Map<K, SaprodontiaNum> saprodontiaNumMap) {
        SaprodontiaNum build = new SaprodontiaNum()
                .build(statConclusionList).ratioNotSymbol().ratio();
        saprodontiaNumMap.put(key, build);
    }

    public static  <K> void getHeightAndWeightNum(K key, List<StatConclusion> statConclusionList, Map<K, HeightAndWeightNum> heightAndWeightNumMap) {
        HeightAndWeightNum build = new HeightAndWeightNum()
                .build(statConclusionList).ratioNotSymbol().ratio();
        heightAndWeightNumMap.put(key, build);
    }

    public static <K> void getBloodPressureAndSpinalCurvatureNum(K key, List<StatConclusion> statConclusionList, Map<K, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap) {
        BloodPressureAndSpinalCurvatureNum build = new BloodPressureAndSpinalCurvatureNum()
                .build(statConclusionList).ratioNotSymbol().ratio();
        bloodPressureAndSpinalCurvatureNumMap.put(key, build);
    }

}
