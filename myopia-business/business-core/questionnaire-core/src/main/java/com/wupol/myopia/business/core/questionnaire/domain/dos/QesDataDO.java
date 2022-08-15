package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * qes序号
 *
 * @author Simple4H
 */
@Getter
@Setter
public class QesDataDO implements Serializable {

    /**
     * 选项Id
     */
    private String optionId;

    /**
     * qes序号
     */
    private String qesSerialNumber;

    /**
     * 展示序号
     */
    private String showSerialNumber;

    /**
     * qes字段
     */
    private String qesField;
}
