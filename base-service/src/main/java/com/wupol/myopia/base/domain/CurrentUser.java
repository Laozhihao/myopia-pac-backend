package com.wupol.myopia.base.domain;

import lombok.Data;

/**
 * @Author HaoHao
 * @Date 2020/12/26
 **/
@Data
public class CurrentUser {

    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 机构组织ID（如政府部门ID、学校ID、医院ID）
     */
    private Integer orgId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别：0-男、1-女
     */
    private Integer gender;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户名（账号）
     */
    private String username;

    /**
     * 系统编号
     */
    private Integer systemCode;

    /**
     * 状态：0-启用 1-禁止 2-删除
     */
    private Integer status;

}
