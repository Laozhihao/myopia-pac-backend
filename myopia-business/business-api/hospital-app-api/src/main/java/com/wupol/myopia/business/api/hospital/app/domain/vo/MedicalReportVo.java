package com.wupol.myopia.business.api.hospital.app.domain.vo;

import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 医院的检查报告
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class MedicalReportVo extends MedicalReport {
    /** 医院名称 */
    private String hospitalName;
    /** 医生名称 */
    private String doctorName;
    /** 影像列表 */
    private List<String> imageUrlList;
}
