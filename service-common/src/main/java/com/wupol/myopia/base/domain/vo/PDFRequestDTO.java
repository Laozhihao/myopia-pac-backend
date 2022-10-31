package com.wupol.myopia.base.domain.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 请求转换DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
@Accessors(chain = true)
public class PDFRequestDTO {

    /**
     * 压缩包文件名
     */
    private String zipFileName;

    /**
     * 详情
     */
    private List<Item> items;

    /**
     * lockKey
     */
    private String lockKey;

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Item {

        /**
         * 请求URL
         */
        private String url;

        /**
         * 文件名
         */
        private String fileName;

    }
}
