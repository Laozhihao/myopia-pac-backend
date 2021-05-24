package com.wupol.myopia.business.core.screening.organization.domain.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 筛查机构人员查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningOrganizationStaffQueryDTO extends ScreeningOrganizationStaffDTO {
    /** 身份证 */
    private String idCardLike;
    /** 姓名 */
    private String nameLike;
    /** 手机号码 */
    private String phoneLike;
}
