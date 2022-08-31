package com.wupol.myopia.business.aggregation.export.excel.questionnaire.function;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.aggregation.export.excel.constant.RecExportDataTypeEnum;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * 按区域统计rec导出类型
 *
 * @author hang.yuan 2022/7/20 14:34
 */
@Service
public class DistrictStatisticsRecExportType implements ExportType {

    @Autowired
    private ExportTypeFacade exportTypeFacade;

    private static final String KEY = "%s的%s的rec文件";
    private static final String DISTRICT_SCHOOL = "%s各学校rec文件";
    private static final String FILE_EXPORT_REC = "file:export:rec:districtStatisticsRec:%s-%s-%s-%s";


    @Override
    public Integer getType() {
        return ExportTypeConst.DISTRICT_STATISTICS_REC;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return exportTypeFacade.getDistrictKey(exportCondition,KEY);
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return exportTypeFacade.getDistrictKey(exportCondition,KEY);
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(FILE_EXPORT_REC,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getPlanId(),
                exportCondition.getDistrictId(),
                exportCondition.getQuestionnaireType().get(0));
    }

    @Override
    public Map<Integer, String> getQuestionnaireType() {
        return exportTypeFacade.getQuestionnaireType(getType());
    }


    @Override
    public String getFolder(Integer districtId) {
        return exportTypeFacade.getDistrictKey(districtId,DISTRICT_SCHOOL);
    }

    @Override
    public void preProcess(ExportCondition exportCondition) {
        ExportTypeFacade.checkDistrictId(exportCondition);
        exportCondition.setSchoolId(null);
        if (Objects.equals(RecExportDataTypeEnum.ARCHIVE_REC.getCode(),exportCondition.getDataType())){
            exportCondition.setQuestionnaireType(Lists.newArrayList(QuestionnaireTypeEnum.ARCHIVE_REC.getType()));
        }
    }
}
