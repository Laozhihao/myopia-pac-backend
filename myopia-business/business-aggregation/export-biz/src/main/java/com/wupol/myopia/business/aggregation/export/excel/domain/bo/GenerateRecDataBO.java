package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

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

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 学校类型
     */
    private Integer schoolType;

    /**
     * 政府唯一Key
     */
    private String governmentKey;

    /**
     * qes文件地址
     */
    private String qesUrl;

    /**
     * 导出rec文件数据
     */
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
