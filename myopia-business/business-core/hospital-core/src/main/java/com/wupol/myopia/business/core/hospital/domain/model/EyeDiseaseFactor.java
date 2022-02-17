package com.wupol.myopia.business.core.hospital.domain.model;

import com.wupol.myopia.business.core.hospital.domain.interfaces.HasResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 主要高危眼病
 *
 * @Author wulizhou
 * @Date 2022/1/6 20:36
 */
@Data
@Accessors(chain = true)
public class EyeDiseaseFactor implements HasResult {

    /**
     * 出生时早产或低出生体重
     */
    private List<BaseValue> problemsAtBirth;
    /**
     * 曾入住新生儿重症监护病房
     */
    private List<BaseValue> hospitalizationAtBirth;
    /**
     * 遗传性眼病家族史
     */
    private List<BaseValue> geneticDisease;
    /**
     * 母亲孕期宫内感染
     */
    private List<BaseValue> intrauterineInfection ;
    /**
     * 颅面及颜面畸形
     */
    private List<BaseValue> facialDeformity;
    /**
     * 眼部情况
     */
    private List<BaseValue> eyeCondition;
    private Boolean isAbnormal;
    private Integer studentId;
    private Integer doctorId;

}
