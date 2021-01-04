package com.wupol.myopia.business.management.domain.query;


import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 筛查机构查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningOrganizationQuery extends ScreeningOrganization {
    /** 机构id */
    private String orgIdLike;
    /** 机构名 */
    private String nameLike;
    /** 地区编码 */
    private Long code;
}
