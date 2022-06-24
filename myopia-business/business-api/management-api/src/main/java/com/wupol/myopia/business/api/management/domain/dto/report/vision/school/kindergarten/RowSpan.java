package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CommonTable;
import lombok.Getter;
import lombok.Setter;

/**
 * RowSpan
 *
 * @author Simple4H
 */
@Getter
@Setter
public class RowSpan extends CommonTable {

    /**
     * 班级数量
     */
    private Long rowSpan;


}
