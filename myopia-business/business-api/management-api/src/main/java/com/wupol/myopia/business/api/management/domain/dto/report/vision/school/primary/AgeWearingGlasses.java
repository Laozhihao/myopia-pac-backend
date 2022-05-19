package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighAndLow;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年龄矫正戴镜情况情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AgeWearingGlasses {

    /**
     * 学龄段
     */
    private String ageRange;

    /**
     * 信息
     */
    private List<HighAndLow> infoList;

    /**
     * 表格
     */
    private List<LowVisionTable> tables;
}
