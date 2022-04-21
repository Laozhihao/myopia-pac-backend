package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;

/**
 * @Author 钓猫的小鱼
 * @Date 2022/4/21 14:38
 * @Email: shuailong.wu@vistel.cn
 * @Des: 乳牙恒牙扩展类
 */
@Data
public class SaprodontiaDataDODTO {
    /**
     * 乳牙
     */
    private SaprodontiaStat deciduousTooth;
    /**
     * 恒牙
     */
    private SaprodontiaStat permanentTooth;

}
