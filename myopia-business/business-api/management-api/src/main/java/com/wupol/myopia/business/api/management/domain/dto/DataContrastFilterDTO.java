package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.core.common.domain.model.District;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 数据对比筛选项
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DataContrastFilterDTO implements Serializable {

    private List<District> districtList;

    private List<FilterParamsDTO<Integer, String>> schoolAgeList;

    private List<FilterParamsDTO<Integer, String>> schoolList;

    private List<FilterParamsDTO<String, String>> schoolGradeList;

    private List<String> schoolClassList;

}
