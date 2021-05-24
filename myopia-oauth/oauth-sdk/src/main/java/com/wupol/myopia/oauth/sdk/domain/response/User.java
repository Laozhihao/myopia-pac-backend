package com.wupol.myopia.oauth.sdk.domain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 身份证号码
     */
    private String idCard;

    /**
     * 用户名（账号）
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否领导：0-否、1-是
     */
    private Integer isLeader;

    /**
     * 系统编号
     */
    private Integer systemCode;

    /**
     * 创建人
     */
    private Integer createUserId;

    /**
     * 状态：0-启用 1-禁止 2-删除
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 最后登录时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    /**
     * 用户类型：0-平台管理员、1-非平台管理员
     */
    private Integer userType;

    /**
     * 创建人真实姓名
     **/
    private String createUserName;

    /**
     * 用户拥有的所有角色
     **/
    private List<Role> roles;

}
