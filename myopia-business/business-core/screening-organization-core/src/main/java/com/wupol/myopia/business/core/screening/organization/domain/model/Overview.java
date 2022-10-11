package com.wupol.myopia.business.core.screening.organization.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wupol.myopia.business.core.common.domain.model.Cooperation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
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
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 部门id
     */
    private Integer govDeptId;

    /**
     * 总览机构名称
     */
    @NotBlank(message = "总览机构名称不能为空")
    @Length(max = 20)
    private String name;

    /**
     * 联系人
     */
    @Length(max = 15)
    private String contactPerson;

    /**
     * 联系方式
     */
    @Length(max = 11)
    private String phone;

    /**
     * 说明
     */
    @Length(max = 50)
    private String illustrate;

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
    private Integer configType;

    /**
     * 医院服务类型（配置），0：居民健康系统(默认)、1：0-6岁眼保健、2：0-6岁眼保健+居民健康系统
     */
    private Integer hospitalServiceType;

    /**
     * 医院限制数量
     */
    private Integer hospitalLimitNum;

    /**
     * 筛查机构配置 0-省级配置 1-单点配置 2-VS666 3-单点配置+VS666
     */
    private Integer screeningOrganizationConfigType;

    /**
     * 筛查机构限制数量
     */
    private Integer screeningOrganizationLimitNum;

    /**
     * 学校配置 0-默认配置
     */
    private Integer schoolConfigType;

    /**
     * 学校限制数量
     */
    @Max(value = 999, message = "超过学校限制数量")
    private Integer schoolLimitNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


}
