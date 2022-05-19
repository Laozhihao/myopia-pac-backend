package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighAndLow;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.GenderWearingTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同学龄段近视矫正情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolAgeWearingGlasses {

    /**
     * 信息
     */
    private List<HighAndLow> infoList;

    /**
     * 表格
     */
    private List<GenderWearingTable> tables;



}
