package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dto.SlitLampDataDTO;
import lombok.Data;

/**
 * 复合检查数据（眼位、裂隙灯、眼底）
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Data
public class CompositeExamDataDTO {
    /**
     * 眼位
     **/
    private OcularInspectionDataDTO ocularInspectionData;
    /**
     * 裂隙灯
     **/
    private SlitLampDataDTO slitLampData;
    /**
     * 眼底
     **/
    private FundusDataDTO fundusData;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;
}
