package com.wupol.myopia.business.core.hospital.util;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.hospital.constant.CheckEnum;
import com.wupol.myopia.business.core.hospital.domain.interfaces.HasResult;
import com.wupol.myopia.business.core.hospital.domain.model.BaseValue;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2022/1/18 10:03
 */
public class PreschoolCheckRecordUtil {

    /**
     * 获取检查结果
     * @param pcr
     * @return TwoTuple<总体结论[0 异常；1 正常], 结论>
     */
    public static TwoTuple<Integer, String> conclusion(PreschoolCheckRecord pcr) {
        List<String> abnormalCheck = new ArrayList<>();
        if (Objects.isNull(pcr) || Objects.isNull(pcr.getMonthAge())) {
            throw new BusinessException("数据异常");
        }
        if (isAbnormal(pcr.getOuterEye())) {
            abnormalCheck.add(CheckEnum.OUTER_EYE.getName());
        }
        if (isAbnormal(pcr.getEyeDiseaseFactor())) {
            abnormalCheck.add(CheckEnum.EYE_DISEASE_FACTOR.getName());
        }
        if (isAbnormal(pcr.getLightReaction())) {
            abnormalCheck.add(CheckEnum.LIGHT_REACTION.getName());
        }
        if (isAbnormal(pcr.getBlinkReflex())) {
            abnormalCheck.add(CheckEnum.BLINK_REFLEX.getName());
        }
        if (isAbnormal(pcr.getRedBallTest())) {
            abnormalCheck.add(CheckEnum.RED_BALL_TEST.getName());
        }
        if (isAbnormal(pcr.getVisualBehaviorObservation())) {
            abnormalCheck.add(CheckEnum.VISUAL_BEHAVIOR_OBSERVATION.getName());
        }
        if (isAbnormal(pcr.getRedReflex())) {
            abnormalCheck.add(CheckEnum.RED_REFLEX.getName());
        }
        if (isAbnormal(pcr.getOcularInspection())) {
            abnormalCheck.add(CheckEnum.OCULAR_INSPECTION.getName());
        }
        if (isAbnormal(pcr.getVisionData())) {
            abnormalCheck.add(CheckEnum.VISION_DATA.getName());
        }
        if (isAbnormal(pcr.getMonocularMaskingAversionTest())) {
            abnormalCheck.add(CheckEnum.MONOCULAR_MASKING_AVERSION_TEST.getName());
        }
        if (isAbnormal(pcr.getRefractionData())) {
            abnormalCheck.add(CheckEnum.REFRACTION_DATA.getName());
        }
        if (CollectionUtils.isEmpty(abnormalCheck)) {
            return TwoTuple.of(PreschoolCheckRecord.STATUS_NORMAL, "正常");
        } else {
            return TwoTuple.of(PreschoolCheckRecord.STATUS_ABNORMAL, "异常：" + abnormalCheck.stream().collect(Collectors.joining("、")));
        }
    }

    /**
     * 是否异常
     * @param hasResult
     * @return true:异常; false:正常
     */
    private static boolean isAbnormal(HasResult hasResult) {
        return Objects.nonNull(hasResult) && hasResult.getIsAbnormal();
    }

    /**
     * 组装转诊结论
     * @param record
     * @return
     */
    public static String referralConclusion(ReferralRecord record) {
        List<String> conclusion = new ArrayList<>();
        String specialMedical = record.getSpecialMedical().stream().map(base -> base.getName() + "未做").collect(Collectors.joining("、"));
        if (StringUtils.isNotBlank(specialMedical)){
            conclusion.add(specialMedical);
        }
        String diseaseMedical = record.getDiseaseMedical().stream().filter(base -> base.getId() > 0).map(BaseValue::getName).collect(Collectors.joining("、"));
        if (StringUtils.isNotBlank(specialMedical)){
            conclusion.add(diseaseMedical);
        }
        return conclusion.stream().collect(Collectors.joining("，"));
    }

}
