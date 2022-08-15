package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * excel信息
 *
 * @author hang.yuan
 * @date 2022/8/15
 */
@Data
public class ExcelInfoBO {
    private String type;
    private List<String> qesFields;
    private List<String> optionIds;
    private Map<String, List<QesDataDO>> subInputMap;
}