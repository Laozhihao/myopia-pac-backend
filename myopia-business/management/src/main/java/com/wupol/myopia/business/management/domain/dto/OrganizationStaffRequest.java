package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 筛查人员列表请求体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class OrganizationStaffRequest {

    /**
     * 筛查机构表id
     */
    @NotNull(message = "机构ID不能为空")
    private Integer screeningOrgId;

    /**
     * 人员名称
     */
    private String name;

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 页数
     */
    private Integer current;

    /**
     * 页码
     */
    private Integer size;
}
