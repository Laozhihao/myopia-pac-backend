package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;

import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021/8/22
 **/
@Data
public abstract class AbstractDiagnosisResult {
    public static final int NORMAL = 0;
    public static final int ABNORMAL = 1;

    /**
     * 初步诊断结果：0-正常、1-（疑似）异常
     */
    private Integer diagnosis;

    /**
     * 诊断结果是否为正常
     *
     * @return boolean
     **/
    public boolean isNormal() {
        // diagnosis为空则默认为正常
        return Objects.isNull(diagnosis) || diagnosis == 0;
    }
}
