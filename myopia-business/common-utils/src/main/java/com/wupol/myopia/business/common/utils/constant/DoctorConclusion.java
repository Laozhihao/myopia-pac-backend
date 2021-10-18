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
}
