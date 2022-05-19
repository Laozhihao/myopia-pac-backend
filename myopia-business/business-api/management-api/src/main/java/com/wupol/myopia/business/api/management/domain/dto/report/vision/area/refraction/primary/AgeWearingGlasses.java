package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.GenderItemInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.GenderWearingTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年龄段近视矫正情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AgeWearingGlasses {

    /**
     * 年龄段
     */
    private String ageInfo;

    /**
     * 信息
     */
    private List<GenderItemInfo> info;

    /**
     * 表格
     */
    private List<GenderWearingTable> tables;

}
