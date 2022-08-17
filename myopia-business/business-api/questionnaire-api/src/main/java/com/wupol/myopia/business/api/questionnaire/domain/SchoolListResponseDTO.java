package com.wupol.myopia.business.api.questionnaire.domain;

import com.wupol.myopia.business.core.common.domain.model.District;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 学校列表
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolListResponseDTO {

    /**
     * 学校名称
     */
    private String name;

    /**
     * 省代码
     */
    private String provinceNo;

    /**
     * 市代码
     */
    private String cityNo;

    /**
     * 区代码
     */
    private String areaNo;

    /**
     * 片区：1好片、2中片、3差片
     */
    private Integer areaType;

    /**
     * 监测点：1城区、2郊县
     */
    private Integer monitorType;

    /**
     * 区域
     */
    private List<District> district;
}
