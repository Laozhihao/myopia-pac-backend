package com.wupol.myopia.business.api.screening.app.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 眼位数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Data
public class OcularInspectionDataDTO implements Serializable {
    /**
     * 内斜
     */
    private Integer esotropia;
    /**
     * 外斜
     */
    private Integer exotropia;
    /**
     * 垂直位斜视
     */
    private Integer verticalStrabismus;
    /**
     * 初步诊断结果：0-正常、1-（疑似）异常
     */
    private Integer diagnosis;
}
