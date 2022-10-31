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
    private String zipFileName;

    /**
     * 一共需要导出文件数量
     */
    private Integer exportTotal;

    /**
     * 已经成功数量
     */
    private Integer exportCount;

    private List<Integer> fileIds;

    private Integer schoolId;

    /**
     * lockKey
     */
    private String lockKey;
}
