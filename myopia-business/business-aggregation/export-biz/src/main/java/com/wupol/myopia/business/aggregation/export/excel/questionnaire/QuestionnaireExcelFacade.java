package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.domain.ExportQuestionnaireDTO;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.file.QuestionnaireExcel;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 问卷Excel导出门面
 *
 * @author hang.yuan 2022/7/18 11:03
 */
@Service
public class QuestionnaireExcelFacade {

    @Autowired
    private List<QuestionnaireExcel> excelList;

    @Autowired
    private List<ExportType> typeList;

    public Optional<QuestionnaireExcel> getQuestionnaireExcelService(Integer questionnaireType){
        return excelList.stream()
                .filter(service -> Objects.equals(service.getType(),questionnaireType))
                .findFirst();
    }

    public Optional<ExportType> getExportTypeService(Integer exportType){
        return typeList.stream()
                .filter(service -> Objects.equals(service.getType(),exportType))
                .findFirst();
    }


    /**
     * 导出问卷数据
     *
     * @param exportQuestionnaireDTO 导出问卷参数
     */
    public void exportQuestionnaire(ExportQuestionnaireDTO exportQuestionnaireDTO) {
        Assert.notNull(exportQuestionnaireDTO.getExportType(),"导出类型不能为空");
        switch (exportQuestionnaireDTO.getExportType()){
            case 10:
                exportByScreeningRecord(exportQuestionnaireDTO);
                break;
            case 11:
                exportByQuestionnairePage(exportQuestionnaireDTO);
                break;
            case 12:
                exportByQuestionnaireSchool(exportQuestionnaireDTO);
                break;
            case 13:
                exportByDistrictStatistics(exportQuestionnaireDTO);
                break;
            case 14:
                exportBySchoolStatistics(exportQuestionnaireDTO);
                break;
            case 15:
                exportMultiTerminalSchoolScreeningRecord(exportQuestionnaireDTO);
                break;
            default:
                throw new BusinessException("导出类型不存在");
        }

    }




    /**
     * 工作台->机构筛查记录【问卷数据】导出
     *
     * @param exportQuestionnaireDTO 导出问卷参数
     */
    private void exportByScreeningRecord(ExportQuestionnaireDTO exportQuestionnaireDTO) {
        Assert.notNull(exportQuestionnaireDTO.getScreeningPlanId(),"筛查计划ID不能为空");
        Integer schoolId = exportQuestionnaireDTO.getSchoolId();
        if (Objects.isNull(schoolId)){
            //导出整个计划下的筛查数据
            exportByAllScreeningPlan(exportQuestionnaireDTO);
        }else {
            //导出该计划下的学校筛查数据
            exportScreeningPlanBySchoolId(exportQuestionnaireDTO);
        }
    }

    /**
     * 导出该计划下的学校筛查数据
     *
     * @param exportQuestionnaireDTO 导出问卷参数
     */
    private void exportScreeningPlanBySchoolId(ExportQuestionnaireDTO exportQuestionnaireDTO) {

    }

    /**
     * 导出整个计划下的筛查数据
     *
     * @param exportQuestionnaireDTO 导出问卷参数
     */
    private void exportByAllScreeningPlan(ExportQuestionnaireDTO exportQuestionnaireDTO) {

    }

    /**
     * 工作台-问卷管理【页面级按钮：下载问卷数据】
     *
     * @param exportQuestionnaireDTO 导出问卷参数
     */
    private void exportByQuestionnairePage(ExportQuestionnaireDTO exportQuestionnaireDTO) {
    }

    /**
     * 工作台-问卷管理【学校列表操作：下载问卷数据】
     *
     * @param exportQuestionnaireDTO 导出问卷参数
     */
    private void exportByQuestionnaireSchool(ExportQuestionnaireDTO exportQuestionnaireDTO) {
    }


    /**
     * 统计报表-按区域统计
     *
     * @param exportQuestionnaireDTO 导出问卷参数
     */
    private void exportByDistrictStatistics(ExportQuestionnaireDTO exportQuestionnaireDTO) {

    }

    /**
     * 统计报表-按学校统计
     *
     * @param exportQuestionnaireDTO 导出问卷参数
     */
    private void exportBySchoolStatistics(ExportQuestionnaireDTO exportQuestionnaireDTO) {

    }

    /**
     * 多端管理-学校管理-筛查记录【问卷导出】
     *
     * @param exportQuestionnaireDTO 导出问卷参数
     */
    private void exportMultiTerminalSchoolScreeningRecord(ExportQuestionnaireDTO exportQuestionnaireDTO) {

    }


}
