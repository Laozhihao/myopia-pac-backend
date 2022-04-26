package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author HaoHao
 * @Date 2022/4/25
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class ArchiveVisionDataDO extends VisionDataDO {
    /**
     * 医生签名图片访问地址
     */
    private String signPicUrl;
}
