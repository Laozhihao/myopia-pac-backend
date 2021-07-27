package com.wupol.myopia.oauth.sdk.domain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.oauth.sdk.domain.request.RoleDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Integer id;

    /**
     * 机构组织ID（如政府部门ID、学校ID、医院ID）
     */
    private Integer orgId;

    /**
     * 英文名
     */
    private String enName;

    /**
     * 中文名
     */
    private String chName;

    /**
     * 角色类型：0-admin、1-机构管理员、2-普通用户
     */
    private Integer roleType;

    /**
     * 创建人
     */
    private Integer createUserId;

    /**
     * 系统编号
     */
    private Integer systemCode;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建用户名
     */
    private String createUserName;

    public RoleDTO convertToRoleDTO() {
        RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(this, roleDTO);
        return roleDTO;
    }
}
