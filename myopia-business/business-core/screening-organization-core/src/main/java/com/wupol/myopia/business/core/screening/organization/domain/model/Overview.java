package com.wupol.myopia.business.core.screening.organization.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wupol.myopia.business.core.common.domain.model.Cooperation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 总览机构信息表
 *
 * @Author wulizhou
 * @Date 2022-02-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_overview")
public class Overview extends Cooperation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 总览机构名称
     */
    private String name;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系方式
     */
    private String phone;

    /**
     * 说明
     */
    private String explain;

    /**
     * 行政区域ID
     */
    private Integer districtId;

    /**
     * 行政区域json
     */
    private String districtDetail;

    /**
     * 配置类型，0：配置筛查机构、1：配置医院、2：配置筛查机构+医院
     */
    private Boolean configType;

    /**
     * 医院服务类型（配置），0：居民健康系统(默认)、1：0-6岁眼保健、2：0-6岁眼保健+居民健康系统
     */
    private Boolean hospitalServiceType;

    /**
     * 医院限制数量
     */
    private Integer hospitalLimitNum;

    /**
     * 筛查机构配置 0-省级配置 1-单点配置
     */
    private Integer screeningOrganizationConfigType;

    /**
     * 筛查机构限制数量
     */
    private Integer screeningOrganizationLimitNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


}
