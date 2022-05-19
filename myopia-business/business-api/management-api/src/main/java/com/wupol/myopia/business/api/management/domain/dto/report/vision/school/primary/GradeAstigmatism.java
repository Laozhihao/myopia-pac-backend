package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighAndLow;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年级近视（散光）情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GradeAstigmatism {

    /**
     * 信息
     */
    private List<HighAndLow> info;

    /**
     * 表格
     */
    private List<AstigmatismTable> tables;
}
