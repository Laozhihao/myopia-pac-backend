package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author HaoHao
 * @Date 2022/4/26
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class ArchiveComputerOptometryDO extends ComputerOptometryDO {
    /**
     * 签名图片访问地址
     */
    private String signPicUrl;
}
