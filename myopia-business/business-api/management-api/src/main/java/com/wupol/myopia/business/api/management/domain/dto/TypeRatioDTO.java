package com.wupol.myopia.business.api.management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2021/6/10 10:54
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TypeRatioDTO {

    private String key;
    private Number num;
    private Float ratio;

    public static TypeRatioDTO getInstance(String key, Number num, Float ratio) {
        return new TypeRatioDTO(key, num, ratio);
    }

}
