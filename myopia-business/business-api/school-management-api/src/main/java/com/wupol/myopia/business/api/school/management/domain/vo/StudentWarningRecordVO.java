package com.wupol.myopia.business.api.school.management.domain.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.aggregation.student.domain.vo.StudentWarningArchiveVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 学生预警跟踪记录
 *
 * @author hang.yuan 2022/9/16 18:00
 */
@Data
public class StudentWarningRecordVO implements Serializable {

    /**
     * 学生信息
     */
    private StudentInfo studentInfo;
    /**
     * 预警跟踪记录集合
     */
    private IPage<StudentWarningArchiveVO> pageData;

    @Data
    @Accessors(chain = true)
    public static class StudentInfo implements Serializable {
        /**
         * 学号
         */
        private String sno;

        /**
         * 年级名称
         */
        private String gradeName;

        /**
         * 班级名称
         */
        private String className;
        /**
         * 性别 0-男 1-女
         */
        private Integer gender;
        /**
         * 学生姓名
         */
        private String name;
    }

}
