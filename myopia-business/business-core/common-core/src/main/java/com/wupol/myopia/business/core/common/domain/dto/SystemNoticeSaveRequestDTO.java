package com.wupol.myopia.business.core.common.domain.dto;

import com.wupol.myopia.business.core.common.domain.model.SystemCodeDO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 保存系统更新通知DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SystemNoticeSaveRequestDTO {

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    private String comment;

    /**
     * 发布平台
     */
    @NotNull(message = "发布平台不能为空")
    private List<Integer> platform;

    public List<SystemCodeDO> platform2SystemCode() {
        return platform.stream().map(s -> {
            SystemCodeDO systemCodeDO = new SystemCodeDO();
            systemCodeDO.setSystemCode(s);
            return systemCodeDO;
        }).collect(Collectors.toList());
    }
}
