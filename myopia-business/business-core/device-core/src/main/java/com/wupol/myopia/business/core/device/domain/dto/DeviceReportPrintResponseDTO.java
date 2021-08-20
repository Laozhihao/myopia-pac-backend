package com.wupol.myopia.business.core.device.domain.dto;

import com.wupol.myopia.business.core.common.domain.dto.SuggestHospitalDTO;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
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
public class DeviceReportPrintResponseDTO extends DeviceScreeningData {

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

    /**
     * 模板类型 1-VS666模板1
     */
    private Integer templateType;

    /**
     * 左眼球镜-展示使用
     */
    private Double leftSphDisplay;

    /**
     * 右眼球镜-展示使用
     */
    private Double rightSphDisplay;

    /**
     * 左眼柱镜-展示使用
     */
    private Double leftCylDisplay;

    /**
     * 右眼柱镜-展示使用
     */
    private Double rightCylDisplay;
}
