package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.OcularInspectionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 眼位数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class OcularInspectionDataDTO extends ScreeningResultBasicData {
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
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        OcularInspectionDataDO ocularInspectionData = new OcularInspectionDataDO()
                .setEsotropia(esotropia)
                .setExotropia(exotropia)
                .setVerticalStrabismus(verticalStrabismus)
                .setDiagnosis(diagnosis)
                .setIsCooperative(isCooperative);
        return new VisionScreeningResult().setOcularInspectionData(ocularInspectionData);
    }
}
