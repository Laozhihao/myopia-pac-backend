package com.wupol.myopia.business.api.management.domain.dto;

import java.util.List;

/**
 * 报告总结
 * @Author wulizhou
 * @Date 2022/12/27 17:18
 */
public class SummaryDTO {

    /**
     * 类型
     */
    private String keyName;

    /**
     * 高占比项名称
     */
    private List<String> itemNameHigh;

    /**
     * 占比（高）
     */
    private String radioHigh;

    /**
     * 低占比项名称
     */
    private List<String> itemNameLow;

    /**
     * 占比（低）
     */
    private String radioLow;

}
