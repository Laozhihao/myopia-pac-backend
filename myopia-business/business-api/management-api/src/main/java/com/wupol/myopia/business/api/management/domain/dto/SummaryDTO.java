package com.wupol.myopia.business.api.management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 报告总结
 * @Author wulizhou
 * @Date 2022/12/27 17:18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDTO {

    /**
     * 名称
     */
    private String keyName;

    /**
     * 最高名称
     */
    private List<String> highName;

    /**
     * 最高百分比
     */
    private Float highRadio;

    /**
     * 最低名称
     */
    private List<String> lowName;

    /**
     * 最低百分比
     */
    private Float lowRadio;

}
