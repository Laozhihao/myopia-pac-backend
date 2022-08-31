package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 生成Excel数据业务实体
 *
 * @author hang.yuan 2022/7/30 11:14
 */
@Data
public class GenerateExcelDataBO {

    /**
     * excel表头信息
     */
    private List<List<String>> head;

    /**
     * excel导出数据
     */
    private Map<Integer,List<List<String>>> dataMap;
}
