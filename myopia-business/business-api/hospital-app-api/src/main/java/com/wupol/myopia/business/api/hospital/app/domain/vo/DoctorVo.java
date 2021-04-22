package com.wupol.myopia.business.api.hospital.app.domain.vo;

import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
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
public class DoctorVO extends DoctorDTO {
    /** 头像url */
    private String avatarUrl;
    /** 签名url */
    private String signUrl;
}
