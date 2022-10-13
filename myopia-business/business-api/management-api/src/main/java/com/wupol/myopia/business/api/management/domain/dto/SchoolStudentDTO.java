package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 学校学生
 *
 * @author hang.yuan 2022/10/13 17:17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SchoolStudentDTO extends SchoolStudent {

    /**
     * 省市县镇（街道）
     */
    private List<Long> townRegionArr;

    /**
     * 详情地址
     */
    private List<Long> regionArr;
}
