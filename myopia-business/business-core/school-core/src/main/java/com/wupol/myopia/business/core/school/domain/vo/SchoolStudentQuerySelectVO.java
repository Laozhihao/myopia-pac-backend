package com.wupol.myopia.business.core.school.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 学校学生查询条件的下拉框数据
 *
 * @author hang.yuan 2022/10/11 11:15
 */
@Data
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
