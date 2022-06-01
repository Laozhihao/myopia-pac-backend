package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年龄段屈光筛查情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AgeRefractive {

    /**
     * 学龄段
     */
    private String ageRange;

    /**
     * 远视储备不足
     */
    private HighLowProportion insufficientInfo;

    /**
     * 屈光不正
     */
    private HighLowProportion refractiveErrorInfo;

    /**
     * 屈光参差
     */
    private HighLowProportion anisometropiaInfo;

    /**
     * 建议就诊
     */
    private HighLowProportion recommendDoctorInfo;

    /**
     * 年龄段图表
     */
    private HorizontalChart ageRefractiveChart;
    
    /**
     * 表格
     */
    private List<RefractiveTable> tables;
}
