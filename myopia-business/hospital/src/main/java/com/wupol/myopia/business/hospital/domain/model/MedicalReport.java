package com.wupol.myopia.business.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wupol.myopia.business.hospital.domain.dos.ReportConclusionDataDO;
import com.wupol.myopia.business.hospital.domain.handler.VisionMedicalRecordTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 医院-检查报告
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("h_medical_report")
public class MedicalReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 配镜情况, 框架镜 */
    public static final Integer GLASSES_SITUATION_COMMON_GLASSES = 1;
    /** 配镜情况, OK眼镜 */
    public static final Integer GLASSES_SITUATION_OK_GLASSES = 2;
    /** 配镜情况, 隐形眼镜 */
    public static final Integer GLASSES_SITUATION_CONTACT_LENS = 3;


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
    /** 配镜情况。1配框架眼镜，2. OK眼镜，3配隐形眼镜 */
    private Integer glassesSituation;
    /** 检查单id */
    private Integer medicalRecordId;
    /** 影像列表 */
    private List<Integer> fileIdList;
    /** 医生诊断内容 */
    private String medicalContent;
    /** 固化的结论数据*/
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ReportConclusionDataDO reportConclusionData;
    /** 创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /** 更新时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


}
