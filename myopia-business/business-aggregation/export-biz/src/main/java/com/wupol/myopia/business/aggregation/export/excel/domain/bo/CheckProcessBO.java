package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 检查条件实体
 *
 * @author hang.yuan 2022/8/28 16:53
 */
@Data
@Accessors(chain = true)
public class CheckProcessBO {

    /**
     * 数据
     */
    private Map<Integer, String> item;
    /**
     * 错误集合
     */
    private List<String> errorItemList;
    /**
     * 筛查编码集合
     */
    private List<String> screeningCodeList;
    /**
     * 重复身份证号集合
     */
    private List<String> idCardList;
    /**
     * 重复护照集合
     */
    private List<String> passportList;
    /**
     * 重复学号集合
     */
    private List<String> snoList;
    /**
     * 筛查计划学校学生集合
     */
    private List<ScreeningPlanSchoolStudent> existPlanSchoolStudentList;
    /**
     * 年级和班级集合
     */
    private Map<String, SchoolGradeExportDTO> gradeMaps;
    /**
     * 学校ID
     */
    private Integer schoolId;
}
