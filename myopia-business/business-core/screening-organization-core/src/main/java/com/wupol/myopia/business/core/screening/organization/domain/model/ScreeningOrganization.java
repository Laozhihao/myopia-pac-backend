package com.wupol.myopia.business.core.screening.organization.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wupol.myopia.base.constant.CooperationTimeTypeEnum;
import com.wupol.myopia.base.constant.CooperationTypeEnum;
import com.wupol.myopia.base.constant.StatusConstant;
import com.wupol.myopia.base.util.BusinessUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.common.utils.handler.DateDeserializer;
import com.wupol.myopia.business.common.utils.interfaces.HasName;
import com.wupol.myopia.business.core.common.domain.model.AddressCode;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrgConfigTypeEnum;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrganizationEnum;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationExportDTO;
import com.wupol.myopia.business.core.screening.organization.domain.handler.NotificationConfigTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

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
public class ScreeningOrganization extends AddressCode implements Serializable, HasName {

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
     * 行政区域JSON
     */
    private String districtDetail;

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
    private Integer configType;

    /**
     * 联系方式
     */
    private String phone;

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
     * 告知书配置
     */
    @TableField(typeHandler = NotificationConfigTypeHandler.class)
    private NotificationConfig notificationConfig;

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
     * 结果通知配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ResultNoticeConfig resultNoticeConfig;

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
     * 筛查人员统计
     */
    @TableField(exist = false)
    private Integer staffCount;

    /**
     * 筛查次数
     */
    @TableField(exist = false)
    private Long screeningTime;

    /**
     * 行政区域详细名称
     */
    @TableField(exist = false)
    private String districtDetailName;

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

    /**
     * 合作是否开始
     * @return
     */
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
     * 检验合作数据是否合法
     * @return
     */
    public boolean checkCooperation() {
        return BusinessUtil.checkCooperation(cooperationType, cooperationTimeType, cooperationStartTime, cooperationEndTime);
    }

    /**
     * 初始化合作默认信息
     */
    public void initCooperationInfo() {
        Date date = new Date();
        cooperationType = CooperationTypeEnum.COOPERATION_TYPE_COOPERATE.getType();                         // 合作
        cooperationTimeType = CooperationTimeTypeEnum.COOPERATION_TIME_TYPE_1_YEAR.getType();               // 合作1年
        cooperationStartTime = DateUtils.addMinutes(date, -5);
        cooperationEndTime = DateUtil.getLastMinute(DateUtils.addYears(date, 1));
    }

    /**
     * 清除合作信息
     */
    public void clearCooperationInfo() {
        cooperationType = null;
        cooperationTimeType = null;
        cooperationStartTime = null;
        cooperationEndTime = null;
    }

    /**
     * 转化成ScreeningOrganizationExportDTO
     *
     * @return ScreeningOrganizationExportDTO
     */
    public ScreeningOrganizationExportDTO parseFromScreeningOrg() {
        return new ScreeningOrganizationExportDTO()
                .setName(name)
                .setType(ScreeningOrganizationEnum.getTypeName(type))
                .setPhone(phone)
                .setCooperationType(CooperationTimeTypeEnum.getCooperationTimeTypeDesc(cooperationType, cooperationTimeType, cooperationStartTime, cooperationEndTime))
                .setCooperationRemainTime(getCooperationRemainTime())
                .setCooperationStartTime(Objects.nonNull(cooperationStartTime) ? DateFormatUtil.format(cooperationStartTime, DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND) : StringUtils.EMPTY)
                .setCooperationEndTime(Objects.nonNull(cooperationEndTime) ? DateFormatUtil.format(cooperationEndTime, DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND) : StringUtils.EMPTY)
                .setConfigType(ScreeningOrgConfigTypeEnum.getTypeName(configType))
                .setRemark(remark)
                .setCreateTime(DateFormatUtil.format(createTime, DateFormatUtil.FORMAT_DETAIL_TIME));
    }

}
