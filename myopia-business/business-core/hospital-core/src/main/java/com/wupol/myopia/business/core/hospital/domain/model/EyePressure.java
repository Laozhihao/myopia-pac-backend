package com.wupol.myopia.business.core.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wupol.myopia.business.core.hospital.domain.handler.DiseaseTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 眼压
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class EyePressure {
    /** 学生id */
    private Integer studentId;
    /** 右眼压 */
    private String rightPressure;
    /** 左眼压 */
    private String leftPressure;

}
