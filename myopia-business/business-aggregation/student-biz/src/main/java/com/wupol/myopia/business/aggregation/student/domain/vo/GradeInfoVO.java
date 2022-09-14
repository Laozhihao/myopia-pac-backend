package com.wupol.myopia.business.aggregation.student.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 年级信息
 *
 * @author hang.yuan 2022/9/13 14:33
 */
@Data
@Accessors(chain = true)
public class GradeInfoVO implements Serializable {

    /**
     * 未选中的年级信息
     */
    private List<GradeInfo> noSelectList;
    /**
     * 选中的年级信息
     */
    private List<GradeInfo> selectList;

    @Data
    public static class GradeInfo{
        /**
         * 年级ID
         */
        private Integer gradeId;
        /**
         * 年级名称
         */
        private String gradeName;
        /**
         * 学生数
         */
        private Integer studentNum;
    }
}
