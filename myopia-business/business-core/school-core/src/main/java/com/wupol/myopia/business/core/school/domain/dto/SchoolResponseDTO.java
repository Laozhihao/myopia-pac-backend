package com.wupol.myopia.business.core.school.domain.dto;

import com.wupol.myopia.business.core.school.domain.model.School;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校返回类
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SchoolResponseDTO extends School {
    /**
     * 账号
     */
    private String accountNo;

    /**
     * 是否能更新
     */
    private boolean canUpdate = false;

    /**
     * 行政区域名
     */
    private String districtName;

    /**
     * 地址详情
     */
    private String addressDetail;

    /**
     * 是否重置密码
     */
    private Boolean updatePassword = false;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否已有计划
     */
    private Boolean alreadyHavePlan = false;
}