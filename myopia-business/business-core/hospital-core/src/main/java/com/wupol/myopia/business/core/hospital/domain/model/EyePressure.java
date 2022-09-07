package com.wupol.myopia.business.core.hospital.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 眼压
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class EyePressure implements Serializable {
    /** 学生id */
    private Integer studentId;
    /** 右眼压 */
    private String rightPressure;
    /** 左眼压 */
    private String leftPressure;

}
