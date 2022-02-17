package com.wupol.myopia.business.core.hospital.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wupol.myopia.base.domain.vo.FamilyInfoVO;
import com.wupol.myopia.business.core.hospital.domain.interfaces.HasParentInfoInterface;
import com.wupol.myopia.business.core.hospital.domain.model.ReceiptList;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @Author wulizhou
 * @Date 2022/1/4 20:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReceiptDTO extends ReceiptList implements HasParentInfoInterface {

    /**
     * 学生名称
     */
    private String studentName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 编号
     */
    private String recordNo;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 家长姓名
     */
    private String parentName;

    /**
     * 联系方式
     */
    private String parentPhone;

    /**
     * 出具回执单医院
     */
    private String fromHospital;

    /**
     * 出具回执单医师
     */
    private String fromDoctor;

    /**
     * 月龄
     */
    private Integer monthAge;

    /**
     * 家庭信息
     */
    @JsonIgnore
    @TableField(typeHandler = JacksonTypeHandler.class)
    private FamilyInfoVO familyInfo;

}
