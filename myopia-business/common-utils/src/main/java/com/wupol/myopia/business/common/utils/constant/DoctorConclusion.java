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
    public final String MIDDLE_RESULT_1 = "裸眼远视力下降，戴镜远视力下降。建议：请及时到医疗机构复查，建议就诊。";

    /**
     * 矫正视力大于4.9
     */
    public final String MIDDLE_RESULT_2 = "裸眼远视力下降，戴镜远视力≥4.9。建议：请3个月或半年1次检查裸眼视力和戴镜视力。";

    /**
     * (小学生 && 0<=SE<2 && Cyl <1.5) || (初中生、高中、职业高中 && -0.5<=SE<3 && Cyl <1.5)
     */
    public final String MIDDLE_RESULT_3 = "裸眼远视力下降，视功能可能异常。建议：请到医疗机构接受检查，明确诊断并及时采取措施，建议就诊。";

    /**
     * (小学生 && !(0 <= SE < 2)) || (初中生、高中、职业高中 && (Cyl >= 1.5 || !(-0.5 <= SE < 3)))
     */
    public final String MIDDLE_RESULT_4 = "裸眼视力下降，合并较为明显的屈光不正或眼病。建议：到医疗机构明确诊断及时矫治疗，建议就诊。";

    /**
     * SE >= 0
     */
    public final String MIDDLE_RESULT_5 = "裸眼远视力≥4.9，目前尚无近视高危因素。建议：6～12 个月复查。";

    /**
     * SE >= 2 && age >=6
     */
    public final String MIDDLE_RESULT_6 = "裸眼远视力≥4.9，目前尚 无近视高危因素。但远视度数较高，可能存在远视高位因素。建议：建议请到医疗机构接受检查，建议就诊。";

    /**
     * SE < 0
     */
    public final String MIDDLE_RESULT_7 = "裸眼远视力≥4.9，可能存在近视高危因素。建议：1）严格注意用 眼卫生；2）到医疗机构接受检查了解是否可能发展为近视，建议就诊。";

    /**
     * 设备报告-结论-屈光不正
     */
    public final String CONCLUSION_DEVICE_REFRACTIVE_ERROR = "屈光不正。";

    /**
     * 设备报告-屈光不正
     */
    public final String DEVICE_REFRACTIVE_ERROR = "建议：请到医疗机构接受检查，明确诊断并及时采取措施。";

    /**
     * 设备报告-结论-屈光正常-远视储备正常
     */
    public final String CONCLUSION_DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_NORMAL = "双眼视力正常。";

    /**
     * 设备报告-屈光正常-远视储备正常
     */
    public final String DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_NORMAL = "建议：每隔6~12个月，带孩子到正规医疗机构进行视力检查。平时注意眼睛休息避免劳累熬夜，尽量少玩手机上网，多户外，多远近交替看东西避免眼睛疲劳，读书学习姿势要端正光线要充足，养成做眼睛保健操的习惯！";

    /**
     * 设备报告-结论-屈光正常-远视储备不足
     */
    public final String CONCLUSION_DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_ERROR = "远视储备不足。";

    /**
     * 设备报告-屈光正常-远视储备不足
     */
    public final String DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_ERROR = "建议：请6~12个月，带孩子到正规医疗机构进行视力检查，了解是否可能发展为近视。平时注意眼睛休息避免劳累熬夜，尽量少玩手机上网，多户外，多远近交替看东西避免眼睛疲劳，读书学习姿势要端正光线要充足，养成做眼睛保健操的习惯！";


    public final static String KINDERGARTEN_RESULT_1 = "戴镜视力下降，建议及时到医疗机构复查，确定是否需要更换眼镜。建议：建议就诊。";

    public final static String KINDERGARTEN_RESULT_2 = "戴镜视力正常。建议3个月或半年1次检查裸眼视力和戴镜视力，建议：--";

    public final static String KINDERGARTEN_RESULT_3 = "裸眼视力下降，视功能可能异常。建议到医疗机构接受散瞳光检查，明确诊断并采取措施。建议：建议就诊。";

    public final static String KINDERGARTEN_RESULT_4 = "裸眼视力下降，合并较为明显的屈光不正或眼病。建议到医疗机构明确诊断及时矫治疗。建议：建议就诊。";
}
