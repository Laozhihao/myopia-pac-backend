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
public class JumpIdsDO implements Serializable{

    private static final long serialVersionUID = 3469839273420851595L;
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
        private static final long serialVersionUID = 6565382409227766658L;
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
