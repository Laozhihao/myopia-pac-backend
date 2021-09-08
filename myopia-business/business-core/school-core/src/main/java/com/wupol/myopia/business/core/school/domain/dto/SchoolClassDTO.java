package com.wupol.myopia.business.core.school.domain.dto;

import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 班级数据
 * @author Alix
 * @Date 2020/12/22
 **/

@Data
@Accessors(chain = true)
public class SchoolClassDTO extends SchoolClass {
    /** 年级名称 */
    private String gradeName;

    /**
     * 唯一Id
     */
    private String uniqueId;
}