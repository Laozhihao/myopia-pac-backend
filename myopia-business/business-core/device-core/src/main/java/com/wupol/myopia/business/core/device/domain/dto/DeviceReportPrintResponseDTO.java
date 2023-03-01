package com.wupol.myopia.business.core.device.domain.dto;

import com.wupol.myopia.business.core.common.domain.dto.SuggestHospitalDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 设备打印报告返回体
 *
 * @author Simple4H
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DeviceReportPrintResponseDTO extends DeviceScreeningDataAndOrgDTO {
    /**
     * 医生建议
     */
    private String doctorAdvice;

    /**
     * 医生结论
     */
    private String doctorConclusion;

    /**
     * 建议医院
     */
    private SuggestHospitalDTO suggestHospitalDTO;
}
