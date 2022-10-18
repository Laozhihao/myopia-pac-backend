package com.wupol.myopia.business.aggregation.export.service;

import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;

import java.util.List;

/**
 * 示例筛查结果
 *
 * @author Simple4H
 */
public interface IScreeningDataService {

    /**
     * 生成导出文件数据
     * @return
     */
    List generateExportData(List<StatConclusionExportDTO> statConclusionExportDTOs,Boolean isHaiNan);

    /**
     * 获取类型
     *
     * @return 类型 {@link ScreeningTypeEnum}
     */
    Integer getScreeningType();

    /**
     * 获取导出文件class文件
     * @return
     */
    Class getExportClass();
}
