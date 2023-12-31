package com.wupol.myopia.business.api.parent.domain.dto;

import com.wupol.myopia.base.domain.RefractoryResultItems;
import com.wupol.myopia.base.domain.VisionItems;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 眼健康报告
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PreschoolReportDTO extends PreschoolCheckRecordDTO {

    /**
     * 视力检查结果
     */
    private List<VisionItems> visionResultItems;

    /**
     * 验光仪检查结果
     */
    private List<RefractoryResultItems> refractoryResultItems;
}