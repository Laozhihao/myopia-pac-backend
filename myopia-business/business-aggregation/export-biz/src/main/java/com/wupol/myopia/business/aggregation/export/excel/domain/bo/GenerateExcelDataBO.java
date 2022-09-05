package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 生成Excel数据业务实体
 *
 * @author hang.yuan 2022/7/30 11:14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateExcelDataBO {

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 政府唯一Key
     */
    private String governmentKey;

    /**
     * excel导出数据
     */
    private List<JSONObject> dataList;

    /**
     * 记分问题ID集合
     */
    private List<Integer> questionIds;

    public GenerateExcelDataBO(Integer schoolId, List<JSONObject> dataList) {
        this.schoolId = schoolId;
        this.dataList = dataList;
    }

    public GenerateExcelDataBO(String governmentKey, List<JSONObject> dataList) {
        this.governmentKey = governmentKey;
        this.dataList = dataList;
    }
}
