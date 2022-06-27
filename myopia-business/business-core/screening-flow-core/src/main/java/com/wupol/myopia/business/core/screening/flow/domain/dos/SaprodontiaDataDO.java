package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 龋齿
 * @Date 2021/4/06 16:50
 * @Author by xz
 */
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class SaprodontiaDataDO extends AbstractDiagnosisResult implements Serializable {
    /**
     * 上牙床
     */
    private List<SaprodontiaItem> above;

    /**
     * 下牙床
     */
    private List<SaprodontiaItem> underneath;

    @Data
    public static class SaprodontiaItem {
        /**
         * 牙齿编码
         */
        private Integer index;

        /**
         * 乳牙
         */
        private String deciduous;

        /**
         * 恒牙
         */
        private String permanent;
    }
}
