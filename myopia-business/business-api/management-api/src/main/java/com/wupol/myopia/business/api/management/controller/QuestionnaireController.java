package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.domain.ExportQuestionnaireDTO;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 问卷管理
 *
 * @author hang.yuan
 * @date 2022/7/19
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/questionnaire")
public class QuestionnaireController {

    @Resource
    private ExportStrategy exportStrategy;

    /**
     * 导出问卷数据
     *
     * @param exportQuestionnaireDTO 导出问卷参数
     */
    @PostMapping("/exportQuestionnaire")
    public void exportQuestionnaire(@RequestBody ExportQuestionnaireDTO exportQuestionnaireDTO) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        ExportCondition exportCondition = new ExportCondition()
                .setApplyExportFileUserId(currentUser.getId())
                .setPlanId(exportQuestionnaireDTO.getScreeningPlanId())
                .setDistrictId(exportQuestionnaireDTO.getDistrictId())
                .setSchoolId(exportQuestionnaireDTO.getSchoolId())
                .setExportType(exportQuestionnaireDTO.getExportType())
                .setQuestionnaireType(exportQuestionnaireDTO.getQuestionnaireType());

        exportStrategy.doExport(exportCondition, ExportExcelServiceNameConstant.QUESTIONNAIRE_SERVICE);
    }

}
