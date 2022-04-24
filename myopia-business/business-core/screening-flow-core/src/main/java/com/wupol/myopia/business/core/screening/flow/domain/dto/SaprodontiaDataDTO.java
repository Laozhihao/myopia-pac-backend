package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;

/**
 * @author tastyb
 */
@Data
public class SaprodontiaDataDTO {

    /**
     * 乳牙
     */
    private SaprodontiaStat deciduousTooth;
    /**
     * 恒牙
     */
    private SaprodontiaStat permanentTooth;

}
