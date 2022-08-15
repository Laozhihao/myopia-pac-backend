package com.wupol.myopia.rec.server.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

    @NotBlank(message = "txt文件链接不能为空")
    private String txtUrl;

    @NotBlank(message = "rec文件名称")
    private String recName;

}
