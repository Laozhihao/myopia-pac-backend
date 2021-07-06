package com.wupol.myopia.business.core.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wupol.myopia.business.core.hospital.domain.handler.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 医院-检查单
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "h_medical_record",autoResultMap = true)
public class MedicalRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 检查中 */
    public static final Integer STATUS_CHECKING = 0;
    /** 检查完成 */
    public static final Integer STATUS_FINISH = 1;


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 医院id */
    private Integer hospitalId;
    /** 科室id */
    private Integer departmentId;
    /** 学生id */
    @NotBlank(message = "学生id不能为空")
    private Integer studentId;
    /** 医生id */
    private Integer doctorId;
    /** 状态。0检查中，1检查完成 */
    private Integer status;
    /** 问诊内容 */
    @TableField(typeHandler = ConsultationTypeHandler.class)
    private Consultation consultation;
    /** 视力检查 */
    @TableField(typeHandler = VisionMedicalRecordTypeHandler.class)
    private VisionMedicalRecord vision;
    /** 生物测量 */
    @TableField(typeHandler = BiometricsMedicalRecordTypeHandler.class)
    private BiometricsMedicalRecord biometrics;
    /** 屈光检查 */
    @TableField(typeHandler = DiopterMedicalRecordTypeHandler.class)
    private DiopterMedicalRecord diopter;
    /** 角膜地形图 */
    @TableField(typeHandler = ToscaMedicalRecordTypeHandler.class)
    private ToscaMedicalRecord tosca;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

    /** 检查单是否已经完成 */
    @JsonIgnore
    public Boolean isFinish() {
        return Objects.nonNull(status) && STATUS_FINISH.equals(status);
    }

  }
