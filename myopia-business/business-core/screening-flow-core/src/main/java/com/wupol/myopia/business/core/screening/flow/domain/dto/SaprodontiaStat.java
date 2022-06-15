package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;

/**
 * 龋齿统计
 *
 * @Author HaoHao
 * @Date 2022/4/18
 **/
@Data
public class SaprodontiaStat {
    /**
     * 为龋（d/D）的牙齿总数
     */
    private Integer dCount;
    /**
     * 为失（m/M）的牙齿总数
     */
    private Integer mCount;
    /**
     * 为补（f/F）的牙齿总数
     */
    private Integer fCount;
}
