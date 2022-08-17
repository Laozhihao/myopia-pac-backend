package com.wupol.myopia.business.core.school.domain.dto;

import com.wupol.myopia.business.core.school.domain.model.School;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 新增学校DTO
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class SaveSchoolRequestDTO extends School {

    private List<BatchSaveGradeRequestDTO> batchSaveGradeList;
}
