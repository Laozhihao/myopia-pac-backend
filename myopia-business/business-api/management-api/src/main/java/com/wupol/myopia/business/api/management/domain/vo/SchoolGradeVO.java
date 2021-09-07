package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 年级数据（包括班级）
 * @author Alix
 * @Date 2020/12/22
 **/

@Data
@Accessors(chain = true)
public class SchoolGradeVO extends SchoolGrade {
    /** 班级列表 */
    private List<SchoolClassDTO> classes;

    /**
     * 唯一Id
     */
    private String uniqueID;
}