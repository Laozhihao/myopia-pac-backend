package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 跳转题目
 *
 * @author Simple4H
 */
@Getter
@Setter
public class JumpIdsDO {

    /**
     * 选项Id
     */
    private String optionId;

    /**
     * 跳转题目Id
     */
    private List<JumpIdItem> jumpIds;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static
    class JumpIdItem implements Serializable {
        /**
         * 跳转Id
         */
        private Integer jumpId;

        /**
         * 描述
         */
        private String serialNumber;
    }
}
