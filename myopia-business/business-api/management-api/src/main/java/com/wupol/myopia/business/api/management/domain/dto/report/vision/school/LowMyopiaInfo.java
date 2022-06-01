package com.wupol.myopia.business.api.management.domain.dto.report.vision.school;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.PrimaryLowVisionInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AgeLowVision;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.GenderLowVision;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.GradeLowVision;
import lombok.Getter;
import lombok.Setter;

/**
 * 视力低下情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class LowMyopiaInfo {

    /**
     * 信息
     */
    private PrimaryLowVisionInfo info;

    /**
     * 不同性别视力低下情况
     */
    private GenderLowVision genderLowVision;

    /**
     * 不同年级不同程度视力情况
     */
    private GradeLowVision gradeLowVision;

    /**
     * 不同年龄视力低下情况
     */
    private AgeLowVision ageLowVision;
}
