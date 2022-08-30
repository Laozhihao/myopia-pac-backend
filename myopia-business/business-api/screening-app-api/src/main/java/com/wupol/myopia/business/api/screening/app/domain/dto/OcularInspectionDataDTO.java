package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dos.OcularInspectionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 眼位数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class OcularInspectionDataDTO extends ScreeningResultBasicData implements Serializable {
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
    private List<String> eyeDiseases;
    /**
     * 初步诊断结果：0-正常、1-（疑似）异常
     */
    private Integer diagnosis;

    public static OcularInspectionDataDTO getInstance(OcularInspectionDataDO ocularInspectionDataDO) {
        if (Objects.isNull(ocularInspectionDataDO)) {
            return null;
        }
        OcularInspectionDataDTO ocularInspectionDataDTO = new OcularInspectionDataDTO();
        ocularInspectionDataDTO.setEsotropia(ocularInspectionDataDO.getEsotropia());
        ocularInspectionDataDTO.setExotropia(ocularInspectionDataDO.getExotropia());
        ocularInspectionDataDTO.setVerticalStrabismus(ocularInspectionDataDO.getVerticalStrabismus());
        ocularInspectionDataDTO.setMeasureMethod(ocularInspectionDataDO.getMeasureMethod());
        ocularInspectionDataDTO.setEyeDiseases(ocularInspectionDataDO.getEyeDiseases());
        ocularInspectionDataDTO.setDiagnosis(ocularInspectionDataDO.getDiagnosis());
        return ocularInspectionDataDTO;
    }

    /**
     * 数据是否有效，只用于标准版，不作用于海南版本
     */
    public boolean isValid() {
        return ObjectUtils.anyNotNull(measureMethod, eyeDiseases);
    }

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        OcularInspectionDataDO ocularInspectionDataDO = new OcularInspectionDataDO();
        ocularInspectionDataDO.setEsotropia(esotropia)
                .setExotropia(exotropia)
                .setEyeDiseases(eyeDiseases)
                .setMeasureMethod(measureMethod)
                .setIsCooperative(getIsCooperative())
                .setVerticalStrabismus(verticalStrabismus);
        ocularInspectionDataDO.setCreateUserId(getCreateUserId());
        ocularInspectionDataDO.setDiagnosis(diagnosis);
        ocularInspectionDataDO.setUpdateTime(getUpdateTime());
        return visionScreeningResult;
    }

    @Override
    public String getDataType() {
        return ScreeningConstant.SCREENING_DATA_TYPE_OCULAR_INSPECTION;
    }
}
