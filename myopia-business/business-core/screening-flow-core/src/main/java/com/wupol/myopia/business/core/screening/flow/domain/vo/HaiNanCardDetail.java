package com.wupol.myopia.business.core.screening.flow.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 海南省学生眼疾病筛查单
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HaiNanCardDetail {

    /**
     * 初步检查
     */
    private PreliminaryInspection preliminaryInspection;

    /**
     * 筛查结果
     */
    private ScreeningResults screeningResults;


    static class PreliminaryInspection {

    }

    static class ScreeningResults {

    }
}
