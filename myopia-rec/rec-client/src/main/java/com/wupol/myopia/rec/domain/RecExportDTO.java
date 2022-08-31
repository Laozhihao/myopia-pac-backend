package com.wupol.myopia.rec.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * 导出REC传输实体
 *
 * @author hang.yuan 2022/8/9 16:19
 */
@Data
public class RecExportDTO implements Serializable {

    @NotBlank(message = "qes文件链接不能为空")
    private String qesUrl;

    /**
     * txt文件链接
     */
    private String txtUrl;

    @NotBlank(message = "rec文件名称")
    private String recName;

    /**
     * 导出数据
     */
    private List<String> dataList;
}
