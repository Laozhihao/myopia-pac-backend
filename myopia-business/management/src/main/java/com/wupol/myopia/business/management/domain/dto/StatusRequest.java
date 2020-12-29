package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 更新状态实体类
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StatusRequest {

    private Integer id;

    private Integer userId;

    private Integer status;
}
