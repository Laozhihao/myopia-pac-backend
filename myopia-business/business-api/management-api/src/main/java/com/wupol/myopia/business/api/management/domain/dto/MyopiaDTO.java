package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2021/6/9 11:58
 */
@Data
@Accessors(chain = true)
public class MyopiaDTO extends NumAndRatio {

    /**
     * 统计维度，学校名或学段名/年级名的key值
     */
    private String name;

    /**
     * 人数
     */
    private Integer statNum;

    /**
     * 学校数量，仅在name为学段时有用
     */
    private Integer schoolNum;

}
