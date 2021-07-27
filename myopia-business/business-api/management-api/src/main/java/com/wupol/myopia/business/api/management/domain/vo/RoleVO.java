package com.wupol.myopia.business.api.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.oauth.sdk.domain.response.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 角色
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    public RoleVO(Role role) {
        BeanUtils.copyProperties(role, this);
    }

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
     * 角色类型：0-平台管理员角色、1-政府部门人员角色、2-筛查机构角色
     */
    private Integer roleType;

    /**
     * 创建人
     */
    private Integer createUserId;

    /**
     * 状态：0-启用 1-禁止 2-删除
     */
    private Integer status;

    /**
     * 系统编号
     */
    private Integer systemCode;

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
     * 所属部门ID集
     */
    private List<Integer> orgIds;

    /**
     * 行政区ID
     */
    private Integer districtId;

    /**
     * 行政区明细
     */
    private List<District> districtDetail;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 部门名称
     */
    private String orgName;

    /**
     * 当前页码
     **/
    private Integer current;

    /**
     * 每页条数
     **/
    private Integer size;
}
