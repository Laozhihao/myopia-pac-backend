package com.wupol.myopia.business.core.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wupol.myopia.base.constant.CooperationTimeTypeEnum;
import com.wupol.myopia.base.constant.StatusConstant;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.common.utils.annotation.CheckTimeInterval;
import com.wupol.myopia.business.common.utils.handler.DateDeserializer;
import com.wupol.myopia.business.core.common.domain.model.AddressCode;
import com.wupol.myopia.business.core.hospital.constant.HospitalEnum;
import com.wupol.myopia.business.core.hospital.constant.HospitalLevelEnum;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalExportDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 医院表
 *
 * @Author HaoHao
 * @Date 2020-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_hospital")
@CheckTimeInterval(beginTime = "cooperationStartTime", endTime = "cooperationEndTime", message = "开始时间不能晚于结束时间")
public class Hospital extends AddressCode implements Serializable {

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
     * 行政区域-省Code
     */
    private Integer districtProvinceCode;

    /**
     * 行政区域JSON
     */
    private String districtDetail;

    /**
     * 医院名称
     */
    @NotBlank(message = "医院名称不能为空")
    private String name;

    /**
     * 等级 0-一甲,1-一乙,2-一丙,3-二甲,4-二乙,5-二丙,6-三特,7-三甲,8-三乙,9-三丙 10-其他
     */
    @NotNull(message = "医院等级不能为空")
    private Integer level;

    /**
     * 等级描述
     */
    private String levelDesc;

    /**
     * 医院固定电话
     */
    @Pattern(regexp = RegularUtils.REGULAR_TELEPHONE, message = "固定电话格式错误")
    private String telephone;

    /**
     * 医院类型 0-定点医院 1-非定点医院
     */
    @NotNull(message = "医院医院类型不能为空")
    private Integer type;

    /**
     * 医院性质 0-公立 1-私立
     */
    @NotNull(message = "医院医院性质不能为空")
    private Integer kind;

    /**
     * 详细地址
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String address;

    /**
     * 头像
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer avatarFileId;

    /**
     * 说明
     */
    private String remark;

    /**
     * 是否合作医院 0-否 1-是
     */
    private Integer isCooperation;

    /**
     * 状态 0-启用 1-禁止 2-删除
     */
    private Integer status;

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
     * 服务类型（配置），0：居民健康系统(默认)、1：0-6岁眼保健、2：0-6岁眼保健+居民健康系统
     */
    @NotNull(message = "配置类型不能为空")
    private Integer serviceType;

    /**
     * 合作类型 0-合作 1-试用
     */
    @NotNull(message = "合作类型不能为空")
    private Integer cooperationType;

    /**
     * 合作期限类型 -1-自定义 0-30天 1-60天 2-180天 3-1年 4-2年 5-3年
     */
    @NotNull(message = "合作期限类型不能为空")
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
     * 关联筛查机构的ID
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer associateScreeningOrgId;

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
     * 转换成HospitalExportDTO
     *
     * @return HospitalExportDTO
     */
    public HospitalExportDTO parseFromHospital() {
        return new HospitalExportDTO()
                .setName(name)
                .setLevel(HospitalLevelEnum.getLevel(level))
                .setType(HospitalEnum.getTypeName(type))
                .setKind(HospitalEnum.getKindName(kind))
                .setRemark(remark)
                .setServiceType(HospitalEnum.getServiceTypeName(serviceType))
                .setCooperationType(CooperationTimeTypeEnum.getCooperationTimeTypeDesc(cooperationType, cooperationTimeType, cooperationStartTime, cooperationEndTime))
                .setCooperationRemainTime(cooperationRemainTime)
                .setCooperationStartTime(Objects.nonNull(cooperationStartTime) ? DateFormatUtil.format(cooperationStartTime, DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND) : StringUtils.EMPTY)
                .setCooperationEndTime(Objects.nonNull(cooperationEndTime) ? DateFormatUtil.format(cooperationEndTime, DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND) : StringUtils.EMPTY)
                .setCreateTime(DateFormatUtil.format(createTime, DateFormatUtil.FORMAT_DETAIL_TIME));
    }

}
