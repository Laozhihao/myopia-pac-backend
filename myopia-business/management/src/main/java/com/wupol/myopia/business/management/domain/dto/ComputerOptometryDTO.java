package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.dos.BiometricDataDO;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description 电脑验光数据
 * @Date 2021/1/22 16:37
 * @Author by jacob
 */
@Data
public class ComputerOptometryDTO extends ScreeningResultBasicData{

    /**
     * 右眼轴位
     */
    private String rAxial;
    /**
     * 左眼轴位
     */
    private String lAxial;
    /**
     * 左眼球镜
     */
    private BigDecimal lSph;
    /**
     * 右眼球镜
     */
    private BigDecimal rSph;
    /**
     * 右眼柱镜
     */
    private BigDecimal rCyl;
    /**
     * 左眼柱镜
     */
    private BigDecimal lCyl;


    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        ComputerOptometryDO.ComputerOptometry leftComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(lAxial).setCyl(lCyl).setSph(lSph).setLateriality(0);
        ComputerOptometryDO.ComputerOptometry rightComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(rAxial).setCyl(rCyl).setSph(rSph).setLateriality(1);
        ComputerOptometryDO computerOptometryDO = new ComputerOptometryDO().setRightEyeData(rightComputerOptometry).setLeftEyeData(leftComputerOptometry);
        return visionScreeningResult.setComputerOptometry(computerOptometryDO);
    }
}

