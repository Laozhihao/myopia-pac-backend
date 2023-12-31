package com.wupol.myopia.business.core.common.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wupol.myopia.base.constant.CooperationTimeTypeEnum;
import com.wupol.myopia.base.constant.CooperationTypeEnum;
import com.wupol.myopia.base.constant.StatusConstant;
import com.wupol.myopia.base.util.BusinessUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.handler.DateDeserializer;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2021/12/28 17:47
 */
@Data
@Accessors(chain = true)
public abstract class Cooperation {

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
     * 合作未开始或合作已结束时状态为禁止
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
        // 合作类型
        cooperationType = CooperationTypeEnum.COOPERATION_TYPE_COOPERATE.getType();
        // 合作1年
        cooperationTimeType = CooperationTimeTypeEnum.COOPERATION_TIME_TYPE_1_YEAR.getType();
        cooperationStartTime = DateUtils.addMinutes(date, -5);
        cooperationEndTime = DateUtil.getLastMinute(DateUtils.addYears(date, 1));
    }

    /**
     * 初始化合作默认信息
     */
    public void initCooperationInfo(Integer cooperationType, Integer cooperationTimeType,
                                    Date cooperationStartTime, Date cooperationEndTime) {
        this.cooperationType = cooperationType;                         // 合作
        this.cooperationTimeType = cooperationTimeType;               // 合作1年
        this.cooperationStartTime = cooperationStartTime;
        this.cooperationEndTime = cooperationEndTime;
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

}
