package com.wupol.myopia.oauth.sdk.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
     * 组织状态：0-启用 1-禁止 2-删除
     */
    private Integer status;

    /**
     * 关联机构组织ID（如政府部门ID、学校ID、医院ID）
     */
    private Integer bindOrgId;

    /**
     * 关联的组织系统编号
     */
    private Integer bindSystemCode;


}
