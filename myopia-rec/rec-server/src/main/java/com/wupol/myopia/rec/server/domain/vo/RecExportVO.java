package com.wupol.myopia.rec.server.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 导出REC响应实体
 * @author hang.yuan 2022/8/10 15:00
 */
@Data
public class RecExportVO implements Serializable {

    /**
     * rec文件链接
     */
    private String recUrl;
    /**
     * rec文件名称
     */
    private String recName;
}
