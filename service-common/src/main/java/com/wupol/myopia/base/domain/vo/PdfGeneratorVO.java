package com.wupol.myopia.base.domain.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    private Integer exportTotal;

    private Integer exportCount;

    private List<Integer> fileIds;

    private Integer schoolId;
}
