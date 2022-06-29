package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.PrimaryOverall;
import lombok.Getter;
import lombok.Setter;

/**
 * 各班筛查数据
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ClassOverall {

    /**
     * 名称
     */
    private String name;

    /**
     * 视力情况
     */
    private PrimaryOverall info;

}


