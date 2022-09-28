package com.wupol.myopia.business.core.school.domain.dto;

import com.wupol.myopia.business.core.school.domain.model.SchoolStaff;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 学校员工列表
 *
 * @author Simple4H
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class SchoolStaffListResponseDTO extends SchoolStaff {
    /**
     * 账号密码
     */
    private String username;
}
