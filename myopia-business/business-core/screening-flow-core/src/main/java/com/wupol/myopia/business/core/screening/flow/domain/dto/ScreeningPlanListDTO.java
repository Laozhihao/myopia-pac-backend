package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 筛查计划列表查询实体
 *
 * @author hang.yuan 2022/9/8 23:23
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningPlanListDTO extends PageRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 筛查机构名称
     */
    private String screeningOrgName;

    /**
     * 筛查业务类型（1:自主筛查，0:协助筛查）
     */
    private Integer screeningBizType;

    /**
     * 筛查业务类型（0:视力筛查，1:常见病）
     */
    private Integer screeningType;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 筛查机构ID集合
     */
    private List<Integer> screeningOrgIds;
}
