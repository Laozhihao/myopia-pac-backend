package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AgeWearingTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.PrimaryWearingInfo;
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


    private PrimaryWearingInfo primaryWearingInfo;

    /**
     * 表格
     */
    private List<AgeWearingTable> tables;


}
