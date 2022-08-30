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

    /**
     * 学校行政区域JSON
     */
    private String schoolDistrictDetail;

    /**
     * 学校行政区域ID
     */
    private Integer schoolDistrictId;

    /**
     * 片区类型：1好片、2中片、3差片
     */
    private Integer schoolAreaType;

    /**
     * 监测点类型：1城区、2郊县
     */
    private Integer schoolMonitorType;
}