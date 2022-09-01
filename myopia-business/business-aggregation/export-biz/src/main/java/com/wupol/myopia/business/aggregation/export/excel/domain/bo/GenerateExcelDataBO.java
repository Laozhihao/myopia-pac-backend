package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

import com.alibaba.fastjson.JSONObject;
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
     * 学校ID
     */
    private Integer schoolId;

    /**
     * excel导出数据
     */
    private List<JSONObject> dataList;
}
