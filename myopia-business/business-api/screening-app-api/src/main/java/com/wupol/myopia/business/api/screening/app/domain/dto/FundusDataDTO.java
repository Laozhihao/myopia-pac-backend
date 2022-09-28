package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dos.FundusDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * 眼底数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class FundusDataDTO extends ScreeningResultBasicData implements Serializable {

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

    public static FundusDataDTO getInstance(FundusDataDO fundusDataDO) {
        if (Objects.isNull(fundusDataDO)) {
            return null;
        }
        FundusDataDTO fundusDataDTO = new FundusDataDTO();
        FundusDataDO.FundusData leftEye = fundusDataDO.getLeftEyeData();
        if (Objects.nonNull(leftEye)) {
            fundusDataDTO.setLeftHasAbnormal(leftEye.getHasAbnormal());
        }
        FundusDataDO.FundusData rightEye = fundusDataDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            fundusDataDTO.setRightHasAbnormal(rightEye.getHasAbnormal());
        }
        fundusDataDTO.setRemark(fundusDataDO.getRemark());
        fundusDataDTO.setIsCooperative(fundusDataDO.getIsCooperative());
        return fundusDataDTO;
    }


    public boolean isValid() {
        return ObjectUtils.anyNotNull(leftHasAbnormal, rightHasAbnormal);
    }

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        FundusDataDO fundusDataDO = new FundusDataDO();
        FundusDataDO.FundusData leftData = new FundusDataDO.FundusData();
        leftData.setLateriality(CommonConst.LEFT_EYE)
                .setHasAbnormal(leftHasAbnormal);
        FundusDataDO.FundusData rightData = new FundusDataDO.FundusData();
        rightData.setLateriality(CommonConst.RIGHT_EYE)
                .setHasAbnormal(rightHasAbnormal);
        fundusDataDO.setLeftEyeData(leftData).setRightEyeData(rightData).setRemark(remark).setIsCooperative(getIsCooperative());
        fundusDataDO.setCreateUserId(getCreateUserId());
        fundusDataDO.setUpdateTime(getUpdateTime());
        visionScreeningResult.setFundusData(fundusDataDO);
        return visionScreeningResult;
    }

    @Override
    public String getDataType() {
        return ScreeningConstant.SCREENING_DATA_TYPE_FUNDUS;
    }
}
