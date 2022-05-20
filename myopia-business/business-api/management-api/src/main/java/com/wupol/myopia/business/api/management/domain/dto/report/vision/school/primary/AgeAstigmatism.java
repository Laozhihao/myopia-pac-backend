package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年龄近视（散光）情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AgeAstigmatism {

    /**
     * 年龄段
     */
    private String ageInfo;

    /**
     * 信息
     */
    private PrimaryAstigmatismInfo info;

    /**
     * 表格
     */
    private List<AstigmatismTable> tables;
}
