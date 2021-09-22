package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.FundusDataDO;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 眼底数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Data
public class FundusDataDTO implements Serializable {

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
        return fundusDataDTO;
    }
}
