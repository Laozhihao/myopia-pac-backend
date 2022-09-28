package com.wupol.myopia.business.common.utils.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 民族实体类
 *
 * @author Simple4H
 */
@Data
public class Nation implements Serializable {

    private String enName;

    private String cnName;

    private Integer code;
}
