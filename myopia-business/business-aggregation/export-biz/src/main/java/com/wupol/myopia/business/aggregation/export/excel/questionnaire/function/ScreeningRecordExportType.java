package com.wupol.myopia.business.aggregation.export.excel.questionnaire.function;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;

/**
 * 机构筛查记录导出类型
 *
 * @author hang.yuan 2022/7/20 14:31
 */
@Service
public class ScreeningRecordExportType implements ExportType {

    @Autowired
    private ExportTypeFacade exportTypeFacade;

    private static final String ALL_KEY = "%s筛查计划下学生问卷数据";
    private static final String SCHOOL_KEY = "%s学生问卷数据";
    private static final String FILE_EXPORT_EXCEL_ALL = "file:export:excel:screeningRecordAll:%s-%s";
    private static final String FILE_EXPORT_EXCEL_SCHOOL = "file:export:excel:screeningRecordSchool:%s-%s-%s";

    @Override
    public Integer getType() {
        return ExportTypeConst.SCREENING_RECORD;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return exportTypeFacade.getOrgOrSchoolKey(exportCondition,ALL_KEY,SCHOOL_KEY);
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return exportTypeFacade.getOrgOrSchoolKey(exportCondition,SCHOOL_KEY,SCHOOL_KEY);
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        Integer schoolId = exportCondition.getSchoolId();
        if (Objects.isNull(schoolId)){
            return String.format(FILE_EXPORT_EXCEL_ALL,exportCondition.getApplyExportFileUserId(),exportCondition.getPlanId());
        }else {
            return String.format(FILE_EXPORT_EXCEL_SCHOOL,exportCondition.getApplyExportFileUserId(),exportCondition.getPlanId(),schoolId);
        }
    }

    @Override
    public Map<Integer, String> getQuestionnaireType() {
        return exportTypeFacade.getQuestionnaireType(getType());
    }


    @Override
    public void preProcess(ExportCondition exportCondition) {
        ExportTypeFacade.checkNoticeIdOrTaskIdOrPlanId(exportCondition.getNotificationId(),exportCondition.getTaskId(),exportCondition.getPlanId());
        if (Objects.isNull(exportCondition.getSchoolId())){
            //导出计划下的
            Assert.notNull(exportCondition.getScreeningOrgId(),"机构ID不能为空");
        }

        exportCondition.setQuestionnaireType(Lists.newArrayList(QuestionnaireTypeEnum.VISION_SPINE.getType(), QuestionnaireConstant.STUDENT_TYPE));
    }
}
