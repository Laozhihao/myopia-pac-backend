package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

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
    private Integer screeningOrgId;

    /**
     * 人员名称
     */
    private String name;

    /**
     * 身份证
     */
    private Integer idCard;

    /**
     * 手机号码
     */
    private Integer mobile;


    /**
     * 页数
     */
    private Integer page;

    /**
     * 页码
     */
    private Integer limit;
}
