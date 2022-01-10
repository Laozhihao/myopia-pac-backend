package com.wupol.myopia.business.core.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 转诊信息表
 *
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("h_referral_record")
public class ReferralRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生id
     */
    private Integer studentId;

    /**
     * 申请时间
     */
    private Date applyTime;

    /**
     * 申请医院id
     */
    private Integer fromHospitalId;

    /**
     * 申请医院名称
     */
    private String fromHospital;

    /**
     * 申请医师id
     */
    private Integer fromDoctorId;

    /**
     * 申请医师名
     */
    private String fromDoctor;

    /**
     * 目标医院id
     */
    private Integer toHospitalId;

    /**
     * 目标医院名称
     */
    private String toHospital;

    /**
     * 目标科室名称
     */
    private String toDepartment;

    /**
     * 未做专项检查
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<BaseValue> specialMedical;

    /**
     * 初筛异常项目
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<BaseValue> diseaseMedical;

    /**
     * 检查结论
     */
    private String conclusion;

    /**
     * 转诊状态[0 待就诊；1 已接诊]
     */
    private Integer referralStatus;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;


}
