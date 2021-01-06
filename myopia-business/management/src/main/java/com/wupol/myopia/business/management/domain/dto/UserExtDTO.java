package com.wupol.myopia.business.management.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表扩展类
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserExtDTO extends UserDTO{

    /**
     * 编号
     */
    private String staffNo;

    /**
     * 筛查机构人员表id
     */
    private Integer staffId;

}
