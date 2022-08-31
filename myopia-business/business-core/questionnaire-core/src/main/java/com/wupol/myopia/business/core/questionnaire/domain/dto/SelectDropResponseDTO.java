package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.wupol.myopia.business.core.questionnaire.domain.dos.DropSelect;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 下拉选项
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SelectDropResponseDTO {

    /**
     * key
     **/
    private String key;

    /**
     * 描述
     **/
    private String desc;

    /**
     * 下拉选项
     */
    private List<DropSelect> dropSelects;
}
