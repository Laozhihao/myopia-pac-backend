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
 * 回执单
 *
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("h_receipt_list")
public class ReceiptList implements Serializable {

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
     * 专项检查情况
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<BaseValue> specialMedical;

    /**
     * 诊断结果
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<BaseValue> medicalResult;

    /**
     * 是否进一步转诊[0 否; 1 是]
     */
    private Integer furtherReferral;

    /**
     * 转诊医院
     */
    private String referralHospital;

    /**
     * 开具回执单医院id
     */
    private Integer fromHospitalId;

    /**
     * 开具回执单医生id
     */
    private Integer fromDoctorId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;


}
