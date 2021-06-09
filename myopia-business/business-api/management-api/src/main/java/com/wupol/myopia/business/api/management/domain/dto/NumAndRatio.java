package com.wupol.myopia.business.api.management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2021/6/9 11:30
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class NumAndRatio {

    private Integer num;
    private Float ratio;

    public static NumAndRatio getInstance(Integer num, Float ratio) {
        return new NumAndRatio(num, ratio);
    }

}
