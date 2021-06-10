package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2021/6/9 11:58
 */
@Data
@Accessors(chain = true)
public class MyopiaDTO extends TypeRatioDTO {

    /**
     * 人数
     */
    private Integer statNum;

    /**
     * 学校数量，仅在学段时有用
     */
    private Long schoolNum;

    /**
     * 排名,仅在学校时有用
     */
    private Integer ranking;

    public static MyopiaDTO getInstance(Integer statNum, Long schoolNum, String key, Number num, Float ratio) {
        MyopiaDTO myopiaDTO = new MyopiaDTO();
        myopiaDTO.setStatNum(statNum).setSchoolNum(schoolNum).setKey(key).setNum(num).setRatio(ratio);
        return myopiaDTO;
    }

    public static MyopiaDTO getInstance(Integer statNum, String key, Number num, Float ratio) {
        return getInstance(statNum, null, key, num, ratio);
    }

}
