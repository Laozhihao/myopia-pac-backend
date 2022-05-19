package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.SchoolAgeLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighAndLow;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年龄不同程度视力情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AgeLowVision {

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
    private List<SchoolAgeLowVisionTable> table;


}
