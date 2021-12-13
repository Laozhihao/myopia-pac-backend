package com.wupol.myopia.oauth.sdk.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * org基本信息表
 *
 * @Author wulizhou
 * @Date 2021-12-06
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 机构组织ID（如政府部门ID、学校ID、医院ID）
     */
    private Integer orgId;

    /**
     * 系统编号
     */
    private Integer systemCode;

    /**
     * 用户类型：0-平台管理员、1-政府人员、2-筛查机构、3-医院管理员
     */
    private Integer userType;

    /**
     * 组织状态：0-启用 1-禁止 2-删除
     */
    private Integer status;

    public Organization(Integer orgId, SystemCode code, UserType type, Integer status) {
        this.orgId = orgId;
        this.systemCode = code.getCode();
        this.userType = type.getType();
        this.status = status;
    }

}
