package com.wupol.myopia.business.core.school.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.interfaces.HasName;
import com.wupol.myopia.business.core.common.domain.model.AddressCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

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
public class School extends AddressCode implements Serializable, HasName {

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
     * 行政区域-省Code
     */
    private Integer districtProvinceCode;

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
     * 合作类型 0-合作 1-试用
     */
    private Integer cooperationType;

    /**
     * 合作期限类型 -1-自定义 0-30天 1-60天 2-180天 3-1年 4-2年 5-3年
     */
    private Integer cooperationTimeType;

    /**
     * 合作开始时间
     */
    private Date cooperationStartTime;

    /**
     * 合作结束时间
     */
    private Date cooperationEndTime;

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

    /**
     * 所属区/县行政区域编号
     */
    private Long districtAreaCode;

    /**
     * 片区类型：1好片、2中片、3差片
     */
    private Integer areaType;

    /**
     * 监测点类型：1城区、2郊县
     */
    private Integer monitorType;

    public Integer getCooperationRemainTime() {
        if (Objects.nonNull(cooperationEndTime)) {
            return Math.max(0, (int) DateUtil.betweenDay(cooperationEndTime, new Date(), true));
        }
        return null;
    }

}
