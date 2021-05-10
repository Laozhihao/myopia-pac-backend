package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 筛查机构人员
 *
 * @Author Chikong
 * @Date 2020/12/22
 **/

@Data
@Accessors(chain = true)
public class ScreeningOrganizationStaffVo extends ScreeningOrganizationStaff {

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String realName;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 身份证
     */
    @Pattern(regexp = RegularUtils.REGULAR_ID_CARD, message = "身份证格式错误")
    @NotBlank(message = "身份证不能为空")
    private String idCard;

    /**
     * 手机
     */
    @Pattern(regexp = RegularUtils.REGULAR_MOBILE, message = "手机号码格式错误")
    @NotBlank(message = "手机不能为空")
    private String phone;

    /**
     * 组织名
     */
    private String organizationName;

}