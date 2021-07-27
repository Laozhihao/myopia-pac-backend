package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 裂隙灯检查数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Data
public class SlitLampDataDTO implements Serializable {

    /**
     * 病变异常组织(左眼)
     */
    private String leftPathologicalTissues;

    /**
     * 病变异常组织(右眼)
     */
    private String rightPathologicalTissues;

    /**
     * 初步诊断结果(左眼)：0-正常、1-（疑似）异常
     */
    private Integer leftDiagnosis;

    /**
     * 初步诊断结果（右眼）：0-正常、1-（疑似）异常
     */
    private Integer rightDiagnosis;

    /**
     * 获取右眼病变组织list
     * @return java.util.List<java.lang.String>
     */
    public List<String> getRightPathologicalTissueList() {
        return this.getPathologicalTissueList(rightPathologicalTissues);
    }

    /**
     * 获取左眼病变组织list
     * @return java.util.List<java.lang.String>
     */
    public List<String> getLeftPathologicalTissueList() {
        return this.getPathologicalTissueList(leftPathologicalTissues);
    }

    /**
     * 获取list
     * @param pathologicalTissueStr 逗号分隔的病变组织字符串
     * @return java.util.List<java.lang.String>
     */
    private List<String> getPathologicalTissueList(String pathologicalTissueStr) {
        if (StringUtils.isBlank(pathologicalTissueStr)) {
            return new ArrayList<>();
        }
        String[] diseaseStringArray = pathologicalTissueStr.split(",");
        return Arrays.asList(diseaseStringArray);
    }

}
