package com.wupol.myopia.business.core.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 专项检查项目
 * @Author wulizhou
 * @Date 2022/1/11 10:03
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SpecialMedical {

    /**
     * 红光反射
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BaseMedicalResult redReflex;

    /**
     * 眼位检查
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BaseMedicalResult ocularInspection;

    /**
     * 单眼遮盖厌恶试验
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BaseMedicalResult monocularMaskingAversionTest;

    /**
     * 屈光检查
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private DiopterMedicalRecord.Diopter refractionData;

}
