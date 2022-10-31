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

    private List<Item> items;

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Item {

        private String url;

        private String fileName;

    }
}
