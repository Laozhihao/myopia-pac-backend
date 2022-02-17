package com.wupol.myopia.business.core.hospital.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 眼保健检查数据基础信息
 * @Author wulizhou
 * @Date 2022/1/6 19:29
 */
@Data
@Accessors(chain = true)
public class BaseValue {

    private Integer id;
    private String name;
    private String remark;

}
