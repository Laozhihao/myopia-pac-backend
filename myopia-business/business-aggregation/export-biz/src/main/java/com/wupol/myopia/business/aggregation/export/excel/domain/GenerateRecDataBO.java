package com.wupol.myopia.business.aggregation.export.excel.domain;

import lombok.Data;

/**
 * 生成rec数据业务实体
 *
 * @author hang.yuan 2022/8/17 11:14
 */
@Data
public class GenerateRecDataBO {

    /**
     * qes文件链接
     */
    private String qesUrl;

    /**
     * txt文件链接
     */
    private String txtUrl;

    /**
     * rec文件名称
     */
    private String recName;
}
