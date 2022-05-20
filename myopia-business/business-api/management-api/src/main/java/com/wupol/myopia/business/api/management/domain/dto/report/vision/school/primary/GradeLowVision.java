package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年级不同程度视力情况
 *
 * @author Simple4H
 */
@Setter
@Getter
public class GradeLowVision {

    /**
     * 信息
     */
    private LowVisionInfo info;

    /**
     * 表格
     */
    private List<LowVisionTable> tables;
}
