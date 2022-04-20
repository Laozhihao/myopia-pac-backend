package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Description 个人隐私项
 * @Date 2021/4/06 16:50
 * @Author by xz
 */
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class PrivacyDataDO extends AbstractDiagnosisResult implements Serializable {
    /**
     * 是否隐私项
     */
    private Boolean hasIncident;

    /**
     * 出现的年龄
     */
    private Integer age;
}
