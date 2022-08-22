package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * qes字段对应答案
 *
 * @author hang.yuan
 * @date 2022/8/21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QesFieldDataBO {

    /**
     * qes字段
     */
    private String qesField;

    /**
     * rec答案
     */
    private String recAnswer;
}