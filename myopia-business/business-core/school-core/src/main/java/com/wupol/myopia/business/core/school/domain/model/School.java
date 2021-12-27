package com.wupol.myopia.business.core.school.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wupol.myopia.base.constant.CooperationTimeTypeEnum;
import com.wupol.myopia.base.constant.StatusConstant;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.annotation.CheckTimeInterval;
import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import com.wupol.myopia.business.common.utils.handler.DateDeserializer;
import com.wupol.myopia.business.common.utils.interfaces.HasName;
import com.wupol.myopia.business.core.common.domain.model.AddressCode;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolExportDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

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
@CheckTimeInterval(beginTime = "cooperationStartTime", endTime = "cooperationEndTime", message = "开始时间不能晚于结束时间")
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
    @JsonDeserialize(using = DateDeserializer.class)
    private Date cooperationStartTime;

    /**
     * 合作结束时间
     */
    @JsonDeserialize(using = DateDeserializer.class)
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

    /**
     * 告知书配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private NotificationConfig notificationConfig;

    @TableField(exist = false)
    private Integer cooperationRemainTime;

    @TableField(exist = false)
    private Integer cooperationStopStatus;

    /**
     * 剩余合作时间，单位：天
     *
     * @return java.lang.Integer
     **/
    public Integer getCooperationRemainTime() {
        return DateUtil.getRemainTime(cooperationStartTime, cooperationEndTime);
    }

    /**
     * 合作是否到期
     * @return
     */
    private boolean isCooperationStop() {
        if (Objects.nonNull(cooperationEndTime)) {
            return cooperationEndTime.getTime() < new Date().getTime();
        }
        return true;
    }

    private boolean isCooperationBegin() {
        if (Objects.nonNull(cooperationStartTime)) {
            return cooperationStartTime.getTime() < new Date().getTime();
        }
        return false;
    }

    /**
     * 合作未开始或合作已结束禁止
     * @return
     */
    public Integer getCooperationStopStatus() {
        return (!isCooperationBegin()) || isCooperationStop() ? StatusConstant.DISABLE : StatusConstant.ENABLE;
    }

    /**
     * 转化成SchoolExportDTO
     *
     * @return SchoolExportDTO
     */
    public SchoolExportDTO parseFromSchoolExcel() {
        return new SchoolExportDTO()
                .setNo(schoolNo)
                .setName(name)
                .setKind(SchoolEnum.getKindName(kind))
                .setType(SchoolEnum.getTypeName(type))
                .setRemark(remark)
                .setCooperationType(CooperationTimeTypeEnum.getCooperationTimeTypeDesc(cooperationType, cooperationTimeType, cooperationStartTime, cooperationEndTime))
                .setCooperationRemainTime(getCooperationRemainTime())
                .setCooperationStartTime(Objects.nonNull(cooperationStartTime) ? DateFormatUtil.format(cooperationStartTime, DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND) : StringUtils.EMPTY)
                .setCooperationEndTime(Objects.nonNull(cooperationEndTime) ? DateFormatUtil.format(cooperationEndTime, DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND) : StringUtils.EMPTY)
                .setCreateTime(DateFormatUtil.format(createTime, DateFormatUtil.FORMAT_DETAIL_TIME));
    }

}
