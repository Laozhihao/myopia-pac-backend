package com.wupol.myopia.business.core.school.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 学校学生查询条件的下拉框数据
 *
 * @author hang.yuan 2022/10/11 11:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolStudentQuerySelectVO implements Serializable {

    /**
     * 年份下拉框值
     */
    private List<SelectValue> yearList;
    /**
     * 戴镜类型下拉框值
     */
    private List<SelectValue> glassesTypeList;
    /**
     * 视力类型下拉框值
     */
    private List<SelectValue> visionTypeList;
    /**
     * 屈光类型下拉框值
     */
    private List<SelectValue> refractionTypeList;



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectValue implements Serializable{
        /**
         * 值
         */
        private Integer value;
        /**
         * 标签
         */
        private String label;
    }


}
