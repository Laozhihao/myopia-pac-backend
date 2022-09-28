package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 33cm眼位数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OcularInspectionDataDO  extends AbstractDiagnosisResult implements Serializable {
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
     * 测量方法：1-交替遮盖法、2-遮盖去遮盖法
     */
    private Integer measureMethod;

    /**
     * 眼部疾病
     */
    private String eyeDiseases;

    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;
}
