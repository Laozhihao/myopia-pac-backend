package com.wupol.myopia.business.api.management.service.report;

import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 百分比
 *
 * @author Simple4H
 */
@Service
public class CountAndProportionService {

    public CountAndProportion zeroWarning(List<StatConclusion> statConclusions, Long total) {
        long zeroWarningCount = statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.ZERO.code)).count();
        return new CountAndProportion(zeroWarningCount, BigDecimalUtil.divide(zeroWarningCount, total));
    }

    public CountAndProportion oneWarning(List<StatConclusion> statConclusions, Long total) {
        long oneWarningCount = statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.ONE.code)).count();
        return new CountAndProportion(oneWarningCount, BigDecimalUtil.divide(oneWarningCount, total));
    }

    public CountAndProportion twoWarning(List<StatConclusion> statConclusions, Long total) {
        long twoWarningCount = statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.TWO.code)).count();
        return new CountAndProportion(twoWarningCount, BigDecimalUtil.divide(twoWarningCount, total));
    }

    public CountAndProportion threeWarning(List<StatConclusion> statConclusions, Long total) {
        long threeWarningCount = statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.THREE.code)).count();
        return new CountAndProportion(threeWarningCount, BigDecimalUtil.divide(threeWarningCount, total));
    }

    public CountAndProportion warning(List<StatConclusion> statConclusions, Long total) {
        long threeWarningCount = statConclusions.stream().filter(s -> !Objects.equals(s.getWarningLevel(), WarningLevel.NORMAL.code)).count();
        return new CountAndProportion(threeWarningCount, BigDecimalUtil.divide(threeWarningCount, total));
    }

    public CountAndProportion male(List<StatConclusion> statConclusions) {
        long oneWarningCount = statConclusions.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.MALE.type)).count();
        return new CountAndProportion(oneWarningCount, BigDecimalUtil.divide(oneWarningCount, (long) statConclusions.size()));
    }

    public CountAndProportion female(List<StatConclusion> statConclusions) {
        long oneWarningCount = statConclusions.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.FEMALE.type)).count();
        return new CountAndProportion(oneWarningCount, BigDecimalUtil.divide(oneWarningCount, (long) statConclusions.size()));
    }

    /**
     * 建议就诊
     *
     * @param statConclusions 统计结果
     *
     * @return CountAndProportion
     */
    public CountAndProportion getRecommendDoctor(List<StatConclusion> statConclusions, Long total) {
        List<StatConclusion> statValidList = statConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList());
        long recommendCount = statValidList.stream().filter(StatConclusion::getIsRecommendVisit).count();
        return new CountAndProportion(recommendCount, BigDecimalUtil.divide(recommendCount, total));
    }

    /**
     * 视力低下
     */
    public CountAndProportion lowVision(List<StatConclusion> statConclusions, Long total) {
        long count = statConclusions.stream().filter(s -> Objects.nonNull(s.getIsLowVision())).filter(StatConclusion::getIsLowVision).count();
        return new CountAndProportion(count, BigDecimalUtil.divide(count, total));
    }

    /**
     * 男-视力低下
     */
    public CountAndProportion mLowVision(List<StatConclusion> statConclusions) {
        long count = statConclusions.stream().filter(s -> Objects.nonNull(s.getIsLowVision())).filter(StatConclusion::getIsLowVision).filter(s -> Objects.equals(s.getGender(), GenderEnum.MALE.type)).count();
        return new CountAndProportion(count, BigDecimalUtil.divide(count, (long) statConclusions.size()));
    }

    /**
     * 女-视力低下
     */
    public CountAndProportion fLowVision(List<StatConclusion> statConclusions) {
        long count = statConclusions.stream().filter(s -> Objects.nonNull(s.getIsLowVision())).filter(s -> Objects.equals(s.getGender(), GenderEnum.FEMALE.type)).filter(StatConclusion::getIsLowVision).count();
        return new CountAndProportion(count, BigDecimalUtil.divide(count, (long) statConclusions.size()));
    }

    /**
     * 轻度视力低下
     */
    public CountAndProportion lightLowVision(List<StatConclusion> statConclusions, Long total) {
        long count = statConclusions.stream().filter(s -> Objects.equals(s.getLowVisionLevel(), LowVisionLevelEnum.LOW_VISION_LEVEL_LIGHT.code)).count();
        return new CountAndProportion(count, BigDecimalUtil.divide(count, total));
    }

    /**
     * 中度视力低下
     */
    public CountAndProportion middleLowVision(List<StatConclusion> statConclusions, Long total) {
        long count = statConclusions.stream().filter(s -> Objects.equals(s.getLowVisionLevel(), LowVisionLevelEnum.LOW_VISION_LEVEL_MIDDLE.code)).count();
        return new CountAndProportion(count, BigDecimalUtil.divide(count, total));
    }

    /**
     * 重度视力低下
     */
    public CountAndProportion highLowVision(List<StatConclusion> statConclusions, Long total) {
        long count = statConclusions.stream().filter(s -> Objects.equals(s.getLowVisionLevel(), LowVisionLevelEnum.LOW_VISION_LEVEL_HIGH.code)).count();
        return new CountAndProportion(count, BigDecimalUtil.divide(count, total));
    }

    /**
     * 学龄段视力低下
     */
    public CountAndProportion schoolAgeLowVision(List<StatConclusion> statConclusions, Integer schoolAge, Long total) {
        if (CollectionUtils.isEmpty(statConclusions)) {
            return new CountAndProportion();
        }
        long count = statConclusions.stream()
                .filter(s -> Objects.equals(s.getSchoolAge(), schoolAge))
                .filter(s-> Objects.equals(s.getIsLowVision(), Boolean.TRUE))
                .count();
        return new CountAndProportion(count, BigDecimalUtil.divide(count, total));
    }

    /**
     * 高中学龄段视力低下
     */
    public CountAndProportion seniorAgeLowVision(List<StatConclusion> statConclusions, Long total) {
        long count = statConclusions.stream()
                .filter(s -> Objects.equals(s.getSchoolAge(), SchoolAge.HIGH.code) || Objects.equals(s.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code))
                .filter(s-> Objects.equals(s.getIsLowVision(), Boolean.TRUE))
                .count();
        if (count == 0) {
            return new CountAndProportion();
        }
        return new CountAndProportion(count, BigDecimalUtil.divide(count, total));
    }

    /**
     * 远视储备不足
     *
     * @return CountAndProportion
     */
    public CountAndProportion insufficient(List<StatConclusion> statConclusions, Long total) {
        long insufficientCount = statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.ZERO_SP.code)).count();
        return new CountAndProportion(insufficientCount, BigDecimalUtil.divide(insufficientCount, total));
    }

    /**
     * 屈光参差
     *
     * @return CountAndProportion
     */
    public CountAndProportion anisometropia(List<StatConclusion> statConclusions, Long total) {
        long anisometropiaCount = statConclusions.stream()
                .filter(s -> Objects.equals(s.getIsAnisometropia(), Boolean.TRUE))
                .count();
        return new CountAndProportion(anisometropiaCount, BigDecimalUtil.divide(anisometropiaCount, total));
    }

    /**
     * 所有戴镜
     *
     * @return CountAndProportion
     */
    public CountAndProportion allGlassesType(List<StatConclusion> statConclusions, Long total) {
        long allGlassesCount = statConclusions.stream().filter(s -> !Objects.equals(s.getGlassesType(), GlassesTypeEnum.NOT_WEARING.code)).count();
        return new CountAndProportion(allGlassesCount, BigDecimalUtil.divide(allGlassesCount, total));
    }

    /**
     * 屈光不正
     *
     * @return CountAndProportion
     */
    public CountAndProportion refractiveError(List<StatConclusion> statConclusions, Long total) {
        long refractiveErrorCount = statConclusions.stream().filter(s -> Objects.nonNull(s.getIsRefractiveError())).filter(StatConclusion::getIsRefractiveError).count();
        return new CountAndProportion(refractiveErrorCount, BigDecimalUtil.divide(refractiveErrorCount, total));
    }

    /**
     * 近视
     *
     * @return CountAndProportion
     */
    public CountAndProportion myopia(List<StatConclusion> statConclusions, Long total) {
        Long myopiaCount = statConclusions.stream()
                .filter(s -> Objects.nonNull(s.getIsMyopia())).filter(StatConclusion::getIsMyopia).count();
        return new CountAndProportion(myopiaCount, BigDecimalUtil.divide(myopiaCount, total));
    }

    /**
     * 近视前期数
     *
     * @return CountAndProportion
     */
    public CountAndProportion earlyMyopia(List<StatConclusion> statConclusions, Long total) {
        long earlyMyopiaCount = statConclusions.stream()
                .filter(s -> !Objects.equals(s.getGlassesType(), GlassesTypeEnum.ORTHOKERATOLOGY.code))
                .filter(s -> Objects.equals(s.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code)).count();
        return new CountAndProportion(earlyMyopiaCount, BigDecimalUtil.divide(earlyMyopiaCount, total));
    }


    /**
     * 低度近视数
     *
     * @return CountAndProportion
     */
    public CountAndProportion lightMyopia(List<StatConclusion> statConclusions, Long total) {
        long lightMyopiaCount = statConclusions.stream()
                .filter(s -> !Objects.equals(s.getGlassesType(), GlassesTypeEnum.ORTHOKERATOLOGY.code))
                .filter(s -> Objects.equals(s.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code)).count();
        return new CountAndProportion(lightMyopiaCount, BigDecimalUtil.divide(lightMyopiaCount, total));

    }

    /**
     * 中度近视数
     *
     * @return CountAndProportion
     */
    public CountAndProportion middleMyopia(List<StatConclusion> statConclusions) {
        long lightMyopiaCount = statConclusions.stream()
                .filter(s -> !Objects.equals(s.getGlassesType(), GlassesTypeEnum.ORTHOKERATOLOGY.code))
                .filter(s -> Objects.equals(s.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_MIDDLE.code)).count();
        return new CountAndProportion(lightMyopiaCount, BigDecimalUtil.divide(lightMyopiaCount, (long) statConclusions.size()));

    }

    /**
     * 高度近视数
     *
     * @return CountAndProportion
     */
    public CountAndProportion highMyopia(List<StatConclusion> statConclusions, Long total) {
        long highMyopiaCount = statConclusions.stream()
                .filter(s -> !Objects.equals(s.getGlassesType(), GlassesTypeEnum.ORTHOKERATOLOGY.code))
                .filter(s -> Objects.equals(s.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code)).count();
        return new CountAndProportion(highMyopiaCount, BigDecimalUtil.divide(highMyopiaCount, total));
    }


    /**
     * 近视足矫数
     *
     * @return CountAndProportion
     */
    public CountAndProportion enough(List<StatConclusion> statConclusions, Long total) {
        long enoughCount = statConclusions.stream().filter(s -> Objects.equals(s.getVisionCorrection(), VisionCorrection.ENOUGH_CORRECTED.code)).count();
        return new CountAndProportion(enoughCount, BigDecimalUtil.divide(enoughCount, total));
    }

    /**
     * 夜戴
     *
     * @return CountAndProportion
     */
    public CountAndProportion night(List<StatConclusion> statConclusions,Long total) {
        long enoughCount = statConclusions.stream().filter(s -> Objects.equals(s.getGlassesType(), GlassesTypeEnum.ORTHOKERATOLOGY.code)).count();
        return new CountAndProportion(enoughCount, BigDecimalUtil.divide(enoughCount, total));
    }

    /**
     * 隐形眼镜
     *
     * @return CountAndProportion
     */
    public CountAndProportion contact(List<StatConclusion> statConclusions,Long total) {
        long enoughCount = statConclusions.stream().filter(s -> Objects.equals(s.getGlassesType(), GlassesTypeEnum.CONTACT_LENS.code)).count();
        return new CountAndProportion(enoughCount, BigDecimalUtil.divide(enoughCount, total));
    }

    /**
     * 框架眼镜
     *
     * @return CountAndProportion
     */
    public CountAndProportion glasses(List<StatConclusion> statConclusions, Long total) {
        long enoughCount = statConclusions.stream().filter(s -> Objects.equals(s.getGlassesType(), GlassesTypeEnum.FRAME_GLASSES.code)).count();
        return new CountAndProportion(enoughCount, BigDecimalUtil.divide(enoughCount, total));
    }

    /**
     * 不戴镜
     *
     * @return CountAndProportion
     */
    public CountAndProportion notWearing(List<StatConclusion> statConclusions, Long total) {
        long enoughCount = statConclusions.stream().filter(s -> Objects.equals(s.getGlassesType(), GlassesTypeEnum.NOT_WEARING.code)).count();
        return new CountAndProportion(enoughCount, BigDecimalUtil.divide(enoughCount, total));
    }


    /**
     * 近视未矫数
     *
     * @return CountAndProportion
     */
    public CountAndProportion uncorrected(List<StatConclusion> statConclusions, Long total) {
        long uncorrectedCount = statConclusions.stream().filter(s -> Objects.equals(s.getVisionCorrection(), VisionCorrection.UNCORRECTED.code)).count();
        return new CountAndProportion(uncorrectedCount, BigDecimalUtil.divide(uncorrectedCount, total));

    }

    /**
     * 近视欠矫数
     *
     * @return CountAndProportion
     */
    public CountAndProportion under(List<StatConclusion> statConclusions, Long total) {
        long underCount = statConclusions.stream().filter(s -> Objects.equals(s.getVisionCorrection(), VisionCorrection.UNDER_CORRECTED.code)).count();
        return new CountAndProportion(underCount, BigDecimalUtil.divide(underCount, total));
    }

    /**
     * 未矫、欠矫数
     *
     * @return CountAndProportion
     */
    public CountAndProportion underAndUncorrected(List<StatConclusion> statConclusions) {
        long uncorrectedCount = statConclusions.stream().filter(s -> Objects.equals(s.getVisionCorrection(), VisionCorrection.UNCORRECTED.code) || Objects.equals(s.getVisionCorrection(), VisionCorrection.UNDER_CORRECTED.code)).count();
        return new CountAndProportion(uncorrectedCount, BigDecimalUtil.divide(uncorrectedCount, (long) statConclusions.size()));

    }

    /**
     * 佩戴角膜塑形镜
     *
     * @return CountAndProportion
     */
    public CountAndProportion nightWearing(List<StatConclusion> statConclusions, Long total) {
        long underCount = statConclusions.stream().filter(s -> Objects.equals(s.getGlassesType(), GlassesTypeEnum.ORTHOKERATOLOGY.code)).count();
        return new CountAndProportion(underCount, BigDecimalUtil.divide(underCount, total));
    }

    /**
     * 散光
     *
     * @return CountAndProportion
     */
    public CountAndProportion astigmatism(List<StatConclusion> statConclusions, Long total) {
        Long myopiaCount = statConclusions.stream().filter(s -> Objects.equals(s.getIsAstigmatism(), Boolean.TRUE)).count();
        return new CountAndProportion(myopiaCount, BigDecimalUtil.divide(myopiaCount, total));
    }

    /**
     * 性别视力低下
     */
    public CountAndProportion genderLowVision(List<StatConclusion> statConclusions, Integer genderType) {
        long count = statConclusions.stream()
                .filter(s -> Objects.nonNull(s.getIsLowVision()))
                .filter(StatConclusion::getIsLowVision).filter(s -> Objects.equals(s.getGender(), genderType))
                .count();
        return new CountAndProportion(count, BigDecimalUtil.divide(count, (long) statConclusions.size()));
    }

    /**
     * 性别视力低下
     */
    public CountAndProportion levelLowVision(List<StatConclusion> statConclusions, Integer lowVisionLevel) {
        long count = statConclusions.stream()
                .filter(s -> Objects.equals(s.getLowVisionLevel(), lowVisionLevel))
                .count();
        return new CountAndProportion(count, BigDecimalUtil.divide(count, (long) statConclusions.size()));
    }
}
