package com.wupol.myopia.business.core.screening.organization.domain.dto;

import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author wulizhou
 * @Date 2022/2/22 18:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OverviewDTO extends Overview {

    /**
     * 行政区域名
     */
    private String districtName;

    /**
     * 已绑定的医院数量
     */
    private Long hospitalNum;

    /**
     * 已绑定的筛查机构数量
     */
    private Long screeningOrganizationNum;

    /**
     * 已绑定的学校数量
     */
    private Long schoolNum;

    /**
     * 配置类型List
     */
    private List<String> configTypeList;

}
