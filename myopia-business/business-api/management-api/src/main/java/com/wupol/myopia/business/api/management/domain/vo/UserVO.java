package com.wupol.myopia.business.api.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.api.management.validator.UserAddValidatorGroup;
import com.wupol.myopia.business.api.management.validator.UserUpdateValidatorGroup;
import com.wupol.myopia.business.core.common.domain.model.District;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = {UserUpdateValidatorGroup.class})
    private Integer id;

    /**
     * 机构组织ID（如政府部门ID、学校ID、医院ID）
     */
    private Integer orgId;

    /**
     * 真实姓名
     */
    @NotBlank(message = "姓名不能为空", groups = {UserAddValidatorGroup.class, UserUpdateValidatorGroup.class})
    private String realName;

    /**
     * 性别：0-男、1-女
     */
    private Integer gender;

    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空", groups = {UserAddValidatorGroup.class, UserUpdateValidatorGroup.class})
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
    @NotNull(message = "状态不能为空", groups = {UserAddValidatorGroup.class, UserUpdateValidatorGroup.class})
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;
    /**
     * 创建人真实姓名
     **/
    private String createUserName;

    /**
     * 用户拥有的所有角色
     **/
    private ArrayList<RoleVO> roles;

    /**
     * 机构名称
     **/
    private String orgName;

    /**
     * 所属部门ID集
     **/
    private List<Integer> orgIds;

    /**
     * 当前页码
     **/
    private Integer current;

    /**
     * 每页条数
     **/
    private Integer size;

    /**
     * 角色ID集
     **/
    @NotEmpty(message = "角色ID不能为空", groups = {UserAddValidatorGroup.class, UserUpdateValidatorGroup.class})
    private List<Integer> roleIds;

    /**
     * 用户类型：0-平台管理员、1-非平台管理员
     */
    private Integer userType;

    /**
     * 行政区明细
     */
    private List<District> districtDetail;

    /**
     * 行政区ID
     */
    private Integer districtId;
}
