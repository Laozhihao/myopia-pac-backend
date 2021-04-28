package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.School;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

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
    private Boolean canUpdate;

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

    public Boolean getCanUpdate() {
        return !Objects.isNull(canUpdate) && canUpdate;
    }
}
