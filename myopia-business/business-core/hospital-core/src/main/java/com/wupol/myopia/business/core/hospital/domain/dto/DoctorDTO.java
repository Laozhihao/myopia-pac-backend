package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 医生信息
 * @author Chikong
 * @date 2021-02-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DoctorDTO extends Doctor {

    /** 性别：0-男、1-女 */
    private Integer gender;
    private Integer userId;
    private Integer createUserId;
    /** 手机号码 */
    private String phone;
    private Integer status;
    /** 报告数 */
    private Integer reportCount;
    /** 头像url */
    private String avatarUrl;
    /** 签名url */
    private String signUrl;
}
