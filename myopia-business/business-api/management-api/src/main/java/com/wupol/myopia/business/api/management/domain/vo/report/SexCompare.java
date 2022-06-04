package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
/**
 * 龋齿-不同性别
 * @author hang.yuan
 * @date 2022/6/2
 */
@Data
public class SexCompare{
    /**
     * 前：性别
     */
    private String forwardSex;
    /**
     * 前：占比
     */
    private String forwardRatio;
    /**
     * 后：性别
     */
    private String backSex;
    /**
     * 后：占比
     */
    private String backRatio;
    /**
     * 符号
     */
    private String symbol;
}