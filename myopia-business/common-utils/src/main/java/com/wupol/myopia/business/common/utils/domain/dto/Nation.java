package com.wupol.myopia.business.common.utils.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 民族实体类
 *
 * @author Simple4H
 */
@Data
@NoArgsConstructor
public class Nation implements Serializable {

    private String enName;

    private String cnName;

    private Integer code;

    public Nation(String enName, String cnName, Integer code) {
        this.enName = enName;
        this.cnName = cnName;
        this.code = code;
    }
}
