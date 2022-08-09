package com.wupol.myopia.rec.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 导出REC传输实体
 *
 * @author hang.yuan 2022/8/9 16:19
 */
@Data
public class RecExportDTO {
    @NotNull(message = "qes管理ID不能为空")
    private Integer qesId;
}
