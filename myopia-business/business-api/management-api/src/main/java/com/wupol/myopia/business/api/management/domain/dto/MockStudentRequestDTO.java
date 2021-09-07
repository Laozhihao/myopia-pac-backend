package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 生成虚拟学生DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class MockStudentRequestDTO {

    private Integer studentTotal;

    private List<GradeItem> gradeItem;

    @Getter
    @Setter
    public static class GradeItem {

        private Integer gradeId;

        private String gradeName;

        private List<ClassItem> classItem;
    }

    @Getter
    @Setter
    public static class ClassItem {
        private Integer classId;

        private String className;
    }


}
