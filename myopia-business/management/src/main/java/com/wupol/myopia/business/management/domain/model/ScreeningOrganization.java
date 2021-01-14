package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotNull;

/**
 * 筛查机构表
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_organization")
public class ScreeningOrganization implements Serializable {

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
     * 行政区域ID
     */
    private Integer districtId;

    /**
     * 筛查机构名称
     */
    @NotNull(message = "筛查机构名称不能为空")
    private String name;

    /**
     * 筛查机构类型 0-医院,1-妇幼保健院,2-疾病预防控制中心,3-社区卫生服务中心,4-乡镇卫生院,5-中小学生保健机构,6-其他
     */
    @NotNull(message = "筛查机构类型不能为空")
    private Integer type;

    /**
     * 机构类型描述
     */
    private String typeDesc;

    /**
     * 配置 0-省级配置 1-单点配置
     */
    @NotNull(message = "配置不能为空")
    private Integer configType;

    /**
     * 联系方式
     */
    private String phone;

    /**
     * 省代码
     */
    private Long provinceCode;

    /**
     * 市代码
     */
    @NotNull(message = "市代码不能为空")
    private Long cityCode;

    /**
     * 区代码
     */
    @NotNull(message = "区代码不能为空")
    private Long areaCode;

    /**
     * 镇/乡代码
     */
    @NotNull(message = "镇/乡代码不能为空")
    private Long townCode;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 说明
     */
    private String remark;

    /**
     * 状态 0-启用 1-禁止 2-删除
     */
    private Integer status;

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


}
