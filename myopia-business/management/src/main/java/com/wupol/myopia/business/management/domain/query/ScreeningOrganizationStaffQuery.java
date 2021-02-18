package com.wupol.myopia.business.management.domain.query;


import com.wupol.myopia.business.management.domain.vo.ScreeningOrganizationStaffVo;
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
public class ScreeningOrganizationStaffQuery extends ScreeningOrganizationStaffVo {
    /** 身份证 */
    private String idCardLike;
    /** 姓名 */
    private String nameLike;
    /** 手机号码 */
    private String phoneLike;
}
