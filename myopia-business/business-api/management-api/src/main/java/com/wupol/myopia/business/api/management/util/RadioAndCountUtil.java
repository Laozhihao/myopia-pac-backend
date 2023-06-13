package com.wupol.myopia.business.api.management.util;

import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.RadioAndCount;
import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.VisionCorrection;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 百分比-人数
 *
 * @author Simple4H
 */
@UtilityClass
public class RadioAndCountUtil {

    /**
     * 近视统计
     *
     * @param statConclusion 筛查数据
     * @return RadioAndCount
     */
    public static RadioAndCount getMyopiaRadioAndCount(List<StatConclusion> statConclusion) {
        return getRadioAndCount(statConclusion, s -> Objects.equals(s.getIsMyopia(), Boolean.TRUE));
    }

    /**
     * 视力底下统计
     *
     * @param statConclusion 筛查数据
     * @return RadioAndCount
     */
    public static RadioAndCount getLowVisionRadioAndCount(List<StatConclusion> statConclusion) {
        return getRadioAndCount(statConclusion, s -> Objects.equals(s.getIsLowVision(), Boolean.TRUE));
    }

    /**
     * 建议就诊统计
     *
     * @param statConclusion 筛查数据
     * @return RadioAndCount
     */
    public static RadioAndCount getRecommendDoctorRadioAndCount(List<StatConclusion> statConclusion) {
        return getRadioAndCount(statConclusion, s -> Objects.equals(s.getIsLowVision(), Boolean.TRUE));
    }

    /**
     * 未矫统计
     *
     * @param statConclusion 筛查数据
     * @return RadioAndCount
     */
    public static RadioAndCount getUncorrectedDoctorRadioAndCount(List<StatConclusion> statConclusion) {
        return getRadioAndCount(statConclusion, s -> Objects.equals(s.getVisionCorrection(), VisionCorrection.UNCORRECTED.getCode()));
    }

    /**
     * 欠矫统计
     *
     * @param statConclusion 筛查数据
     * @return RadioAndCount
     */
    public static RadioAndCount getUnderRadioAndCount(List<StatConclusion> statConclusion) {
        return getRadioAndCount(statConclusion, s -> Objects.equals(s.getVisionCorrection(), VisionCorrection.UNDER_CORRECTED.getCode()));
    }

    /**
     * 低度近视统计
     *
     * @param statConclusion 筛查数据
     * @return RadioAndCount
     */
    public static RadioAndCount getLightMyopiaRadioAndCount(List<StatConclusion> statConclusion) {
        return getRadioAndCount(statConclusion, s -> Objects.equals(s.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.getCode()));
    }

    /**
     * 高度近视统计
     *
     * @param statConclusion 筛查数据
     * @return RadioAndCount
     */
    public static RadioAndCount getHighMyopiaRadioAndCount(List<StatConclusion> statConclusion) {
        return getRadioAndCount(statConclusion, s -> Objects.equals(s.getVisionCorrection(), MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.getCode()));
    }

    /**
     * 屈光不正统计
     *
     * @param statConclusion 筛查数据
     * @return RadioAndCount
     */
    public static RadioAndCount getIsRefractiveErrorRadioAndCount(List<StatConclusion> statConclusion) {
        return getRadioAndCount(statConclusion, s -> Objects.equals(s.getIsRefractiveError(), Boolean.TRUE));
    }

    /**
     * 屈光参差统计
     *
     * @param statConclusion 筛查数据
     * @return RadioAndCount
     */
    public static RadioAndCount getIsAnisometropiaRadioAndCount(List<StatConclusion> statConclusion) {
        return getRadioAndCount(statConclusion, s -> Objects.equals(s.getIsAnisometropia(), Boolean.TRUE));
    }

    /**
     * 远视储备不足统计
     *
     * @param statConclusion 筛查数据
     * @return RadioAndCount
     */
    public static RadioAndCount getZeroSpRadioAndCount(List<StatConclusion> statConclusion) {
        return getRadioAndCount(statConclusion, s -> Objects.equals(s.getWarningLevel(), WarningLevel.ZERO_SP.getCode()));
    }


    /**
     * 百分比人数
     *
     * @param statConclusions 筛查数据
     * @param predicate       条件
     * @return 百分比人数
     */
    private static RadioAndCount getRadioAndCount(List<StatConclusion> statConclusions, Predicate<StatConclusion> predicate) {
        long total = statConclusions.size();
        long count = statConclusions.stream().filter(predicate).count();
        return new RadioAndCount(BigDecimalUtil.divide(count, total), count);
    }
}
