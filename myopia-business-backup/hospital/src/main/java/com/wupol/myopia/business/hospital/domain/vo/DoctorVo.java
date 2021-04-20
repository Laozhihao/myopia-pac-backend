package com.wupol.myopia.business.hospital.domain.vo;

import com.wupol.myopia.business.hospital.domain.model.Doctor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 医生信息
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class DoctorVo extends Doctor {
    /** 头像url */
    private String avatarUrl;
    /** 签名url */
    private String signUrl;
    /** 报告数 */
    private Integer reportCount;

}
