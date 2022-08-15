package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 下拉选择
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DropSelect implements Serializable {

    /**
     * 显示的文本
     */
    private String label;

    /**
     * 选中的后台需要存的值
     */
    private String value;
}
