package com.wupol.myopia.business.api.hospital.app.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 图像
 *
 * @author Simple4H
 */
@Getter
@Setter
public class FundusImageDTO {

    /**
     * 文件Id
     */
    private Integer fileId;

    /**
     * 文件URL
     */
    private String fileUrl;
}
