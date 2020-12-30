package com.wupol.myopia.business.management.domain.dto;

import lombok.Data;

/**
 * 筛查人员重置密码
 *
 * @author Simple4H
 */
@Data
public class StaffResetPasswordRequest {

    private Integer staffId;

    private String phone;

    private String idCard;
}
