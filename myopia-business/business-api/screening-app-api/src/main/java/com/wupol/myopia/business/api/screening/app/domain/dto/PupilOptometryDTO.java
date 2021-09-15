package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dos.PupilOptometryDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;

/**
 * 小瞳验光数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class PupilOptometryDTO extends ScreeningResultBasicData {

    /**
     * 右眼轴位
     */
    @JsonProperty("r_axial")
    private BigDecimal rAxial;
    /**
     * 左眼轴位
     */
    @JsonProperty("l_axial")
    private BigDecimal lAxial;
    /**
     * 左眼球镜
     */
    @JsonProperty("l_sph")
    private BigDecimal lSph;
    /**
     * 右眼球镜
     */
    @JsonProperty("r_sph")
    private BigDecimal rSph;
    /**
     * 右眼柱镜
     */
    @JsonProperty("r_cyl")
    private BigDecimal rCyl;
    /**
     * 左眼柱镜
     */
    @JsonProperty("l_cyl")
    private BigDecimal lCyl;
    /**
     * 矫正视力(左眼)
     */
    private BigDecimal leftCorrectedVision;
    /**
     * 矫正视力(右眼)
     */
    private BigDecimal rightCorrectedVision;
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
        PupilOptometryDataDO.PupilOptometryData leftPupilOptometryData = new PupilOptometryDataDO.PupilOptometryData().setAxial(lAxial).setCyl(lCyl).setSph(lSph).setCorrectedVision(leftCorrectedVision).setLateriality(CommonConst.LEFT_EYE);
        PupilOptometryDataDO.PupilOptometryData rightPupilOptometryData = new PupilOptometryDataDO.PupilOptometryData().setAxial(rAxial).setCyl(rCyl).setSph(rSph).setCorrectedVision(rightCorrectedVision).setLateriality(CommonConst.RIGHT_EYE);
        PupilOptometryDataDO pupilOptometryDataDO = new PupilOptometryDataDO().setLeftEyeData(leftPupilOptometryData).setRightEyeData(rightPupilOptometryData).setIsCooperative(isCooperative);
        pupilOptometryDataDO.setDiagnosis(diagnosis);
        pupilOptometryDataDO.setCreateUserId(getCreateUserId());
        return visionScreeningResult.setPupilOptometryData(pupilOptometryDataDO);
    }

    public boolean isValid() {
        return ObjectUtils.anyNotNull(rAxial, lAxial, lSph, rSph, rCyl, lCyl, leftCorrectedVision, rightCorrectedVision);
    }
}

