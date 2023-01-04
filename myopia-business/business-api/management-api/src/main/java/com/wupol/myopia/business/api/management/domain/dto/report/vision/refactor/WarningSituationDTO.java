package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 预警情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class WarningSituationDTO {

    /**
     * 不同年级学生视力预警情况
     */
    private GradeWarningSituation gradeWarningSituation;

    /**
     * 年级学生视力预警情况
     */
    @Getter
    @Setter
    public static class GradeWarningSituation {

        /**
         * 详情
         */
        private List<GradeWarningSituationItem> items;

        /**
         * 表格
         */
        private Object table;

        public static GradeWarningSituation getInstance(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap, List<StatConclusion> statConclusions) {
            WarningSituationDTO.GradeWarningSituation gradeWarningSituation = new WarningSituationDTO.GradeWarningSituation();
            List<WarningSituationDTO.GradeWarningSituationItem> items = gradeCodes.stream().map(s -> {
                List<StatConclusion> gradeStatConclusion = statConclusionGradeMap.getOrDefault(s, new ArrayList<>());
                WarningSituationDTO.GradeWarningSituationItem gradeWarningSituationItem = new WarningSituationDTO.GradeWarningSituationItem();
                gradeWarningSituationItem.setGradeName(GradeCodeEnum.getDesc(s));
                return getWarningSituation(gradeStatConclusion, gradeWarningSituationItem);
            }).collect(Collectors.toList());
            WarningSituationDTO.GradeWarningSituationItem total = new GradeWarningSituationItem();
            total.setGradeName("全校");
            getWarningSituation(statConclusions, total);
            items.add(total);
            gradeWarningSituation.setItems(items);
            gradeWarningSituation.setTable(null);
            return gradeWarningSituation;
        }
    }

    /**
     * 年级学生视力预警情况
     */
    @Getter
    @Setter
    public static class GradeWarningSituationItem extends WarningSituation {

        /**
         * 年级名称
         */
        private String gradeName;
    }

    /**
     * 生视力预警情况
     */
    @Getter
    @Setter
    public static class WarningSituation {

        /**
         * 筛查学生
         */
        private Long screeningStudentNum;

        /**
         * 0级预警人数
         */
        private Long zeroWarningNum;

        /**
         * 0级预警人数百分比
         */
        private Float zeroWarningRatio;

        /**
         * 1级预警人数
         */
        private Long oneWarningNum;

        /**
         * 1级预警人数百分比
         */
        private Float oneWarningRatio;

        /**
         * 2级预警人数
         */
        private Long twoWarningNum;

        /**
         * 2级预警人数百分比
         */
        private Float twoWarningRatio;

        /**
         * 3级预警人数
         */
        private Long threeWarningNum;

        /**
         * 3级预警人数百分比
         */
        private Float threeWarningRatio;
    }

    /**
     * 预警情况
     *
     * @return T
     */
    private static <T extends WarningSituationDTO.WarningSituation> T getWarningSituation(List<StatConclusion> statConclusions, T t) {
        long screeningTotal = statConclusions.size();
        t.setScreeningStudentNum(screeningTotal);
        t.setZeroWarningNum(statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.ONE.code)).count());
        t.setZeroWarningRatio(BigDecimalUtil.divideRadio(t.getZeroWarningNum(), screeningTotal));
        t.setOneWarningNum(warningSituationCount(statConclusions, WarningLevel.ONE.code));
        t.setOneWarningRatio(BigDecimalUtil.divideRadio(t.getOneWarningNum(), screeningTotal));
        t.setTwoWarningNum(warningSituationCount(statConclusions, WarningLevel.TWO.code));
        t.setTwoWarningRatio(BigDecimalUtil.divideRadio(t.getTwoWarningNum(), screeningTotal));
        t.setThreeWarningNum(warningSituationCount(statConclusions, WarningLevel.THREE.code));
        t.setThreeWarningRatio(BigDecimalUtil.divideRadio(t.getThreeWarningNum(), screeningTotal));
        return t;
    }

    /**
     * 预警情况统计
     *
     * @return Long
     */
    private static Long warningSituationCount(List<StatConclusion> statConclusions, Integer type) {
        return statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), type)).count();
    }

}
