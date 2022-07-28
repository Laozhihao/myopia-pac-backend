package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 选项答案
 *
 * @author Simple4H
 */
@Getter
@Setter
public class OptionAnswer implements Serializable {

    private static final long serialVersionUID = -4538461740977702142L;
    /**
     * 选项Id
     */
    private String optionId;

    /**
     * 文本值
     */
    private String text;

    /**
     * 填空值
     */
    private String value;

    /**
     * 填空题
     */
    private List<OptionAnswer> children;
}
