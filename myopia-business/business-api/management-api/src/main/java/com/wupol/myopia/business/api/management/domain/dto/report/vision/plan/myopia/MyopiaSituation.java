package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.myopia;

import lombok.Data;

/**
 * 近视情况
 *
 * @author Simple4H
 */
@Data
public class MyopiaSituation {

    /**
     * 有效筛查人数
     */
    private Long validCount;

    /**
     * 近视人数
     */
    private Long myopiaCount;

    /**
     * 近视率
     */
    private String myopiaRadio;

    /**
     * 小学及以上教育阶段不同性别近视情况
     */
    private Module1 module1;

    /**
     * 小学及以上教育阶段各年级学生近视情况
     */
    private Module2 module2;

    /**
     * 小学及以上教育阶段各年级学生近视分布情况
     */
    private Module21 module21;

    /**
     * 各学校近视率情况
     */
    private Module22 module22;

    /**
     * 各年龄段学生近视情况
     */
    private Object module3;

    /**
     * 各年龄段学生近视情况
     */
    private Object module4;

    /**
     * 小学及以上教育阶段各年龄学生近视情况
     */
    private Object module5;

    /**
     * 小学及以上教育阶段各年级男女学生近视情况
     */
    private Object module6;

    /**
     * 小学及以上教育阶段各年龄学生近视分布情况
     */
    private Object module7;


}
