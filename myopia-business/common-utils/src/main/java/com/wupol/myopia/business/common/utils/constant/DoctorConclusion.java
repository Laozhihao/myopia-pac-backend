package com.wupol.myopia.business.common.utils.constant;

import lombok.experimental.UtilityClass;

/**
 * 医生结论
 *
 * @author Simple4H
 */
@UtilityClass
public class DoctorConclusion {

    /**
     * 矫正视力小于4.9
     */
    public static final String CORRECTED_VISION_LESS_THAN_49 = "裸眼远视力下降，戴镜远视力下降。建议：请及时到医疗机构复查。";

    /**
     * 矫正视力大于4.9
     */
    public static final String CORRECTED_VISION_GREATER_THAN_49 = "裸眼远视力下降，戴镜远视力≥4.9。建议：请3个月或半年1次检查裸眼视力和戴镜视力。";

    /**
     * (小学生 && 0<=SE<2 && Cyl <1.5) || (初中生、高中、职业高中 && -0.5<=SE<3 && Cyl <1.5)
     */
    public static final String VISUAL_FUNCTION_ABNORMAL = "裸眼远视力下降，视功能可能异常。建议：请到医疗机构接受检查，明确诊断并及时采取措施。";

    /**
     * (小学生 && !(0 <= SE < 2)) || (初中生、高中、职业高中 && (Cyl >= 1.5 || !(-0.5 <= SE < 3)))
     */
    public static final String REFRACTIVE_ERROR_SCREENING_POSITIVE = "裸眼远视力≥4.9，目前尚无近视高危因素。建议：1、6-12个月复查。2、6岁儿童SE≥+2.00D，请到医疗机构接受检查。";

    /**
     * SE >= 0
     */
    public static final String NORMAL_SE_GREATER_THAN_0 = "裸眼远视力下降，屈光不正筛查阳性。建议：请到医疗机构接受检查，明确诊断并及时采取措施。";

    /**
     * SE < 0
     */
    public static final String NORMAL_SE_LESS_THAN_0 = "裸眼远视力≥4.9，可能存在近视高危因素。建议：1、严格注意用眼卫生。2、到医疗机构检查了解是否可能发展未近视。";

    /**
     * 正常情况
     */
    public static final String NORMAL_SE = "远视力双眼视力正常，建议：平时注意眼睛休息避免劳累熬夜，尽量少玩手机上网，多户外，多远近交替看东西避免眼睛疲劳，读书学习姿势要端正光线要充足，养成做眼睛保健操的习惯！";

    /**
     * 设备报告-结论-屈光不正
     */
    public static final String CONCLUSION_DEVICE_REFRACTIVE_ERROR = "屈光不正。";

    /**
     * 设备报告-屈光不正
     */
    public static final String DEVICE_REFRACTIVE_ERROR = "建议：请到医疗机构接受检查，明确诊断并及时采取措施。";

    /**
     * 设备报告-结论-屈光正常-远视储备正常
     */
    public static final String CONCLUSION_DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_NORMAL = "双眼视力正常。";

    /**
     * 设备报告-屈光正常-远视储备正常
     */
    public static final String DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_NORMAL = "建议：每隔6~12个月，带孩子到正规医疗机构进行视力检查。平时注意眼睛休息避免劳累熬夜，尽量少玩手机上网，多户外，多远近交替看东西避免眼睛疲劳，读书学习姿势要端正光线要充足，养成做眼睛保健操的习惯！";

    /**
     * 设备报告-结论-屈光正常-远视储备不足
     */
    public static final String CONCLUSION_DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_ERROR = "远视储备不足。";

    /**
     * 设备报告-屈光正常-远视储备不足
     */
    public static final String DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_ERROR = "建议：请6~12个月，带孩子到正规医疗机构进行视力检查，了解是否可能发展为近视。平时注意眼睛休息避免劳累熬夜，尽量少玩手机上网，多户外，多远近交替看东西避免眼睛疲劳，读书学习姿势要端正光线要充足，养成做眼睛保健操的习惯！";
}
