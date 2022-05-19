package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.GenderItemInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同性别矫正戴镜情况情况
 *
 * @author Simple4H
 */

@Getter
@Setter
public class GenderWearingGlasses {

    /**
     * 信息
     */
    private List<GenderItemInfo> info;

    /**
     * 表格
     */
    private List<GenderWearingTable> tables;

}
