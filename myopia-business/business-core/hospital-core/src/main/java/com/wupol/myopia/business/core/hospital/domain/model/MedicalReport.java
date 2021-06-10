package com.wupol.myopia.business.core.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 医院-检查报告
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "h_medical_report",autoResultMap = true)
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
    /** 报告编号. 医院ID+生成日期时分秒（202011111111）+6位数排序（000001开始） */
    private String no;
    /** 医院id */
    @NotBlank(message = "医院id不能为空")
    private Integer hospitalId;
    /** 科室id */
    private Integer departmentId;
    /** 学生id */
    @NotBlank(message = "学生id不能为空")
    private Integer studentId;
    /** 医生id */
    @NotBlank(message = "医生id不能为空")
    private Integer doctorId;
    /** 配镜情况。0无, 1配框架眼镜，2. OK眼镜，3配隐形眼镜 */
    private Integer glassesSituation;
    /** 检查单id */
    private Integer medicalRecordId;
    /** 影像列表 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> imageIdList;
    /** 医生诊断内容 */
    private String medicalContent;
    /** 固化的结论数据*/
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ReportConclusion reportConclusionData;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;


}
