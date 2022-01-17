package com.wupol.myopia.base.domain.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PDF生成Redis对象
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
public class PdfGeneratorVO {

    /**
     * 用户Id
     */
    private Integer userId;

    /**
     * 文件名称
     */
    private String fileName;

    public PdfGeneratorVO(Integer userId, String fileName) {
        this.userId = userId;
        this.fileName = fileName;
    }
}
