package com.wupol.myopia.business.core.screening.flow.domain.vo;

import lombok.Data;

/**
 * @Author  钓猫的小鱼
 * @Date  2022/4/13 20:45
 * @Email: shuailong.wu@vistel.cnø
 * @Des: 复测卡扩展类
 */
@Data
public class ReScreeningCardVO {
    /**
     * 常见病编码
     */
    private String commonDiseasesCode;

    /**
     * 视力筛查
     */
    private VisionVO vision;
    /**
     * 常见病筛查
     */
    private CommonDiseasesVO commonDiseases;

}
