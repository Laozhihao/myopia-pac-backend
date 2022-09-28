package com.wupol.myopia.business.aggregation.screening.domain.bo;

import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 导出业务条件实体
 *
 * @author hang.yuan 2022/9/27 10:17
 */
@Data
@Accessors(chain = true)
public class GeneratorPdfBO implements Serializable {
    /**
     * 机构ID
     */
    private Integer orgId;
    /**
     * 筛查学生ID集合
     */
    private String planStudentIdStr;
    /**
     * 是否学校端
     */
    private Boolean isSchoolClient;
    /**
     * 保存文件路径
     */
    private String fileSaveParentPath;
    /**
     * 学校信息集合
     */
    private Map<Integer, String> schoolMap;
    /**
     * 年级信息集合
     */
    private Map<Integer, SchoolGrade> gradeMap;
    /**
     * 班级信息集合
     */
    private Map<Integer, SchoolClass> classMap;
    /**
     * 计划筛查学校学生信息集合
     */
    private Map<Integer, List<ScreeningStudentDTO>> planGroup;

    /**
     * 筛查计划ID
     */
    private Integer planId;

    /**
     * 学校学生信息集合
     */
    private Map<Integer, List<ScreeningStudentDTO>> schoolGroup;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 学校年级信息集合
     */
    private Map<Integer, List<ScreeningStudentDTO>> gradeGroup;
    /**
     * 年级ID
     */
    private Integer gradeId;

    /**
     * 学校班级级信息集合
     */
    private Map<Integer, List<ScreeningStudentDTO>> classGroup;
    /**
     * 学校班级ID
     */
    private Integer classId;

    /**
     * 文件名称
     */
    private String fileName;

    private PdfResponseDTO pdfResponseDTO;
}
