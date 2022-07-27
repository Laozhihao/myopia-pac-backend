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
    private List<JumpIdsInnerDo> jumpIds;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class JumpIdsInnerDo implements Serializable {
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
