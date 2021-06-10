package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 数据对比筛选项
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DataContrastFilterDTO implements Serializable {

    private List<District> districtList;

    private List<Integer> schoolAgeList;

    private List<School> schoolList;

    private List<SchoolGrade> schoolGradeList;

    private List<SchoolClass> schoolClassList;

}
