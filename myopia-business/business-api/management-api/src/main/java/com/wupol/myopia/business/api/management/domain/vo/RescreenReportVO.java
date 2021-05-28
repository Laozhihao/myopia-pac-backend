package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.screening.flow.domain.model.StatRescreen;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2021/5/21 10:09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RescreenReportVO extends StatRescreen {

    private String orgName;

    private String schoolName;

    /**
     * 质控员
     */
    private String qualityControllerName;

    /**
     * 检测队长
     */
    private String qualityControllerCommander;

}
