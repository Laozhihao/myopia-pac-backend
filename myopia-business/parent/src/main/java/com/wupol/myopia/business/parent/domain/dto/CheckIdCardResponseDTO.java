package com.wupol.myopia.business.parent.domain.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 返回学生信息
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CheckIdCardResponseDTO {

    /**
     * 学生ID
     */
    private Integer studentId;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 性别 1-男 2-女
     */
    private Integer gender;
}
