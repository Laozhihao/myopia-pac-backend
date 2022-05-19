package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighAndLow;

import java.util.List;

/**
 * 不同年级矫正戴镜情况情况
 *
 * @author Simple4H
 */
public class GradeWearingGlasses {

    /**
     * 信息
     */
    private List<HighAndLow> infoList;

    /**
     * 表格
     */
    private List<GenderWearingTable> tables;
}
