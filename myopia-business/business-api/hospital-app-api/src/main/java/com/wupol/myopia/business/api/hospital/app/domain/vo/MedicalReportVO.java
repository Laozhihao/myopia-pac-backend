package com.wupol.myopia.business.api.hospital.app.domain.vo;

import com.wupol.myopia.business.core.hospital.domain.dos.MedicalReportDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 医院的检查报告
 * @author Chikong
 * @date 2021-02-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class MedicalReportVO extends MedicalReportDO {
}
