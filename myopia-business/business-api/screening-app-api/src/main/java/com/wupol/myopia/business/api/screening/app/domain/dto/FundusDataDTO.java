package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.FundusDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 眼底数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class FundusDataDTO extends ScreeningResultBasicData {

    /**
     * 眼底(左眼)：0-未见异常、1-异常
     */
    private Integer leftHasAbnormal;
    /**
     * 眼底（右眼）：0-未见异常、1-异常
     */
    private Integer rightHasAbnormal;
    /**
     * 备注说明
     */
    private String remark;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        FundusDataDO.FundusData leftFundusData = new FundusDataDO.FundusData().setLateriality(0).setHasAbnormal(leftHasAbnormal);
        FundusDataDO.FundusData rightFundusData = new FundusDataDO.FundusData().setLateriality(1).setHasAbnormal(rightHasAbnormal);
        FundusDataDO fundusDataDO = new FundusDataDO().setLeftEyeData(leftFundusData).setRightEyeData(rightFundusData).setIsCooperative(isCooperative).setRemark(remark);
        return new VisionScreeningResult().setFundusData(fundusDataDO);
    }
}
