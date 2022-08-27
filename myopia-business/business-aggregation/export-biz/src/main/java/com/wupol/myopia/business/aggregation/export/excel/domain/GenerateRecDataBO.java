package com.wupol.myopia.business.aggregation.export.excel.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 生成rec数据业务实体
 *
 * @author hang.yuan 2022/8/17 11:14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateRecDataBO {

    private Integer schoolId;

    private String governmentKey;

    private String qesUrl;

    private List<String> dataList;

    public GenerateRecDataBO(Integer schoolId, String qesUrl, List<String> dataList) {
        this.schoolId = schoolId;
        this.qesUrl = qesUrl;
        this.dataList = dataList;
    }

    public GenerateRecDataBO(String governmentKey, String qesUrl, List<String> dataList) {
        this.governmentKey = governmentKey;
        this.qesUrl = qesUrl;
        this.dataList = dataList;
    }
}
