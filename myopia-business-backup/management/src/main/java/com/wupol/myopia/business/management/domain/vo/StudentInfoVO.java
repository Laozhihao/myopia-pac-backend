package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.business.management.domain.dto.StudentClazzDTO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Description
 * @Date 2021/2/1 12:29
 * @Author by Jacob
 */
@Data
@Accessors(chain = true)
public class StudentInfoVO {
    /**
     * clazzName
     */
    private String clazzName;
    /**
     * gradeId
     */
    private Integer clazzId;
    /**
     * gradeName
     */
    private String gradeName;
    /**
     * gradeId
     */
    private Integer gradeId;
    /**
     * schoolId
     */
    private Integer schoolId;
    /**
     * 学校名称
     */
    private String schoolName;
    /**
     * 有几种情况
     */
    private Integer qualified;

    /**
     * reviewsCount
     */
    private Integer reviewsCount;

    /**
     * 设置其他参数
     * @param studentClazzDTO
     */
    public void addOtherInfo(StudentClazzDTO studentClazzDTO) {
        if (studentClazzDTO == null) {
            throw new ManagementUncheckedException("studentClazzDTO 不能为空");
        }
        this.clazzId = studentClazzDTO.getClazzId();
        this.gradeId = studentClazzDTO.getGradeId();
        this.clazzName = studentClazzDTO.getClazzName();
        this.gradeName = studentClazzDTO.getGradeName();
        this.schoolId = studentClazzDTO.getSchoolId();
        this.schoolName = studentClazzDTO.getClazzName();
    }

    public class SchoolInfo {
        /**
         * schoolId
         */
        private Integer schoolId;
        /**
         * 学校名称
         */
        private String schoolName;
    }

    public class GradeInfo {
        /**
         * gradeName
         */
        private String gradeName;
        /**
         * gradeId
         */
        private Integer gradeId;
    }

    public class ClazzInfo {
        /**
         * clazzName
         */
        private String clazzName;
        /**
         * gradeId
         */
        private Integer clazzId;
    }



}
