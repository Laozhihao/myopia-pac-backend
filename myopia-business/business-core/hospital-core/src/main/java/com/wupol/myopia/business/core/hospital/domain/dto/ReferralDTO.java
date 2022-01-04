package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author wulizhou
 * @Date 2022/1/4 20:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ReferralDTO extends ReferralRecord {

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
    private Date birthDay;

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

}
