package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.GenderProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同性别屈光筛查情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SexRefractive {

    /**
     * 远视储备不足
     */
    private GenderProportion insufficientInfo;

    /**
     * 屈光不正
     */
    private GenderProportion refractiveErrorInfo;

    /**
     * 屈光参差
     */
    private GenderProportion anisometropiaInfo;

    /**
     * 建议就诊
     */
    private GenderProportion recommendDoctorInfo;

    /**
     * 表格
     */
    private List<RefractiveTable> tables;

}
