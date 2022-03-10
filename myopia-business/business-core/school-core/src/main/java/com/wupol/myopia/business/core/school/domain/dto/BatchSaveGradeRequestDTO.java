package com.wupol.myopia.business.core.school.domain.dto;

import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 批量新增班级年级DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class BatchSaveGradeRequestDTO {

    private SchoolGrade schoolGrade;

    private List<SchoolClass> schoolClass;
}
