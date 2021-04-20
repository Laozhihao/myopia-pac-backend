package com.wupol.myopia.business.core.school.domian.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.management.interfaces.HasName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 学校表
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_school")
public class School implements Serializable, HasName {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 根据规则创建ID
     */
    private String schoolNo;

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
     * 行政区域JSON
     */
    private String districtDetail;

    /**
     * 学校名称
     */
    private String name;

    /**
     * 学校性质 0-公办 1-私办 2-其他
     */
    private Integer kind;

    /**
     * 学校性质描述 0-公办 1-私办 2-其他
     */
    private String kindDesc;

    /**
     * 寄宿状态 0-全部住校 1-部分住校 2-不住校
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED )
    private Integer lodgeStatus;

    /**
     * 学校类型 0-小学,1-初级中学,2-高级中学,3-完全中学,4-九年一贯制学校,5-十二年一贯制学校,6-职业高中,7其他
     */
    private Integer type;

    /**
     * 省代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED )
    private Long provinceCode;

    /**
     * 市代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long cityCode;

    /**
     * 区代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long areaCode;

    /**
     * 镇/乡代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long townCode;

    /**
     * 详细地址
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
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


    /**
     * 学生统计
     */
    @TableField(exist = false)
    private Integer studentCount;

    /**
     * 筛查次数
     */
    @TableField(exist = false)
    private Long screeningCount;

    /**
     * 创建人
     */
    @TableField(exist = false)
    private String createUser;
}
