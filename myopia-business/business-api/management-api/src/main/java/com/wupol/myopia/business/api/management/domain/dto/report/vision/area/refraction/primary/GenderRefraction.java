package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.GenderItemInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AstigmatismTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 男女屈光筛查情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GenderRefraction {

    /**
     * 信息
     */
    private List<GenderItemInfo> info;

    /**
     * 表格
     */
    private List<AstigmatismTable> tables;
}
