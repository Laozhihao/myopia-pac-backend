package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 重点
 */
@Data
@Accessors(chain = true)
public class FocusObjectsStatisticDTO extends ScreeningBasicResult{

    /**
     * 筛查范围 地区id
     */
    private Long districtId;

    /**
     * 筛查范围 范围名称
     */
    private Long rangeName;

    /**
     * 当前级的数据
     */
    private Item totalData;

    /**
     * 当前级的数据
     */
    private Item currentData;

    /**
     * 下级的数据列表，如果没有的话，为null
     */
    private List<Item>  subordinateDataList;

    @Data
    public static class Item {

        /**
         * 地区id
         */
        private Long districtId;

        /**
         * 筛查范围 范围名称
         */
        private Long rangeName;

        /**
         * 总重点视力对象数
         */
        private Integer focusTargetsNum;

        /**
         * 筛查人数
         */
        private Integer screeningStudentsNum;

        /**
         * 分级预警信息
         */
        private List<WarningInfo.WarningLevelInfo> warningLevelInfoList;
    }
}
