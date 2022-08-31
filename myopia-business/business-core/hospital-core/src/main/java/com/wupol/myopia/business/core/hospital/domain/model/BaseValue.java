package com.wupol.myopia.business.core.hospital.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 眼保健检查数据基础信息
 * @Author wulizhou
 * @Date 2022/1/6 19:29
 */
@Data
@Accessors(chain = true)
public class BaseValue implements Serializable {

    private Integer id;
    private String name;
    private String remark;

}
