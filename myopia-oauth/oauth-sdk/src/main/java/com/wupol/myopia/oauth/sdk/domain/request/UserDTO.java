package com.wupol.myopia.oauth.sdk.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.oauth.sdk.domain.response.Role;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UserDTO extends User {
    /**
     * 开始创建时间（不为空时，endCreateTime也不能为空，且不能比endCreateTime大）
     **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startCreateTime;
    /**
     * 结束创建时间（不为空时，startCreateTime也不能为空，且不能比startCreateTime小）
     **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endCreateTime;
    /**
     * 开始最后登录时间（不为空时，endLastLoginTime也不能为空，且不能比endLastLoginTime大）
     **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startLastLoginTime;
    /**
     * 结束最后登录时间（不为空时，startLastLoginTime也不能为空，且不能比startLastLoginTime小）
     **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endLastLoginTime;
    /**
     * 角色名
     **/
    private String roleName;
    /**
     * 创建人真实姓名
     **/
    private String createUserName;

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
    private List<Integer> roleIds;

    /**
     * 用户拥有的所有角色
     **/
    private List<Role> roles;

    /**
     * 用户ID集合
     **/
    private List<Integer> userIds;

    /**
     * 手机号码集合
     **/
    private List<String> phones;

    /**
     * 身份证号码集合
     **/
    private List<String> idCards;

    /**
     * 筛查机构配置
     */
    private Integer orgConfigType;

    /**
     * 关联筛查机构的ID
     */
    private Integer associateScreeningOrgId;

    /**
     * 用户类型集合
     */
    private List<Integer> userTypes;
}
