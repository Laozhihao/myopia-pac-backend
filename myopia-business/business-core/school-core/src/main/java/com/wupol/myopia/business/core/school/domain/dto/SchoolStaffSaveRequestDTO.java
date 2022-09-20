package com.wupol.myopia.business.core.school.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 学校保存员工
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolStaffSaveRequestDTO {

    /**
     * id
     */
    private Integer id;

    /**
     * 姓名
     */
    private String staffName;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 备注
     */
    private String remark;
}
