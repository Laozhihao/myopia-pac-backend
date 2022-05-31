package com.wupol.myopia.business.core.school.domain.dto;

import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 班级数据
 * @author Alix
 * @Date 2020/12/22
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SchoolClassDTO extends SchoolClass {
    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 唯一Id
     */
    private String uniqueId;

    /**
     * 学校名称
     */
    private String schoolName;
}