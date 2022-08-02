package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import org.springframework.stereotype.Service;

/**
 * 导出地市及区（县）管理部门学校卫生工作调查表
 *
 * @author hang.yuan 2022/7/18 11:23
 */
@Service
public class ExportAreaDistrictSchoolService implements QuestionnaireExcel {

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getType();
    }


}
