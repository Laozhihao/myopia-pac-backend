package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AstigmatismTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年龄段屈光筛查情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AgeRefraction {

    /**
     * 表格
     */
    private List<AstigmatismTable> tables;
}
