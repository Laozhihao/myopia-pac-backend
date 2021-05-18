package com.wupol.myopia.business.api.parent.domain.dos;

import com.wupol.myopia.business.core.screening.flow.domain.dos.BiometricItems;
import com.wupol.myopia.business.core.screening.flow.domain.dos.RefractoryResultItems;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionItems;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 筛查报告统计
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningReportDetailDO {

    /**
     * 筛查机构Id
     */
    private Integer screeningOrgId;

    /**
     * 检查日期
     */
    private Date screeningDate;

    /**
     * 戴镜类型
     */
    private Integer glassesType;

    /**
     * 视力检查结果
     */
    private List<VisionItems> visionResultItems;

    /**
     * 医生建议1 0-正常,1-轻度屈光不正,2-中度屈光不正,3-重度屈光不正
     */
    private Integer doctorAdvice1;

    /**
     * 医生建议2
     */
    private String doctorAdvice2;

    /**
     * 验光仪检查结果
     */
    private List<RefractoryResultItems> refractoryResultItems;

    /**
     * 生物测量
     */
    private List<BiometricItems> biometricItems;

    /**
     * 建议医院
     */
    private SuggestHospitalDO suggestHospital;

}
