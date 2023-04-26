package com.wupol.myopia.business.api.parent.domain.dos;

import com.wupol.myopia.base.domain.RefractoryResultItems;
import com.wupol.myopia.base.domain.VisionItems;
import com.wupol.myopia.business.core.common.domain.dto.SuggestHospitalDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.BiometricItems;
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
     * 医生建议1 5，6-正常,7-轻度屈光不正,8-中度屈光不正,9-重度屈光不正
     */
    private Integer doctorAdvice1;

    /**
     * 医生建议2
     */
    private String doctorAdvice2;

    /**
     * 筛查报告医生是否建议就诊
     */
    private Boolean screeningDoctorAdvice;

    /**
     * 筛查报告医生建议内容
     */
    private String screeningDoctorAdviceContent;

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
    private SuggestHospitalDTO suggestHospital;

}
