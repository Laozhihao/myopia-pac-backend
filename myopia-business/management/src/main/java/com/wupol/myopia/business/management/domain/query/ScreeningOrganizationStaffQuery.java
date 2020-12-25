package com.wupol.myopia.business.management.domain.query;


import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.management.domain.vo.ScreeningOrganizationStaffVo;
import lombok.Data;

/**
 * 筛查机构人员查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@Data
public class ScreeningOrganizationStaffQuery extends ScreeningOrganizationStaffVo {
    /** 身份证 */
    private String idCardLike;
    /** 姓名 */
    private String nameLike;
    /** 手机号码 */
    private String phoneLike;
    /** 机构名 */
    private String orgNameLike;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别 1-男 2-女
     */
    private Integer gender;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 身份证
     */
    private String idCard;
}
