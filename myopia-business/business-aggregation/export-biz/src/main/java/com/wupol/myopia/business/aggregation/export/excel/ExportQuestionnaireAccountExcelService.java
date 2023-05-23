package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.QuestionnaireAccountExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 导出筛查学生问卷账号
 *
 * @author Simple4H
 */
@Service(ExportExcelServiceNameConstant.EXPORT_PLAN_STUDENT_QUESTIONNAIRE_ACCOUNT_SERVICE)
public class ExportQuestionnaireAccountExcelService extends BaseExportExcelFileService {

    @Resource
    private SchoolService schoolService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    @Override
    public List getExcelData(ExportCondition exportCondition) {
        Integer schoolId = exportCondition.getSchoolId();
        Integer planId = exportCondition.getPlanId();
        Integer gradeId = exportCondition.getGradeId();

        List<ScreeningPlanSchoolStudent> planSchoolStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolIdAndGradeId(planId, schoolId, gradeId);

        if (CollectionUtils.isEmpty(planSchoolStudents)) {
            throw new BusinessException("数据为空");
        }
        List<QuestionnaireAccountExportDTO> exportList = new ArrayList<>(planSchoolStudents.size());

        // 年级
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(planSchoolStudents, ScreeningPlanSchoolStudent::getGradeId);

        // 班级
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(planSchoolStudents, ScreeningPlanSchoolStudent::getClassId);

        planSchoolStudents.forEach(student -> {
            QuestionnaireAccountExportDTO exportDTO = new QuestionnaireAccountExportDTO();
            exportDTO.setName(student.getStudentName());
            exportDTO.setGradeName(Objects.nonNull(gradeMap.get(student.getGradeId())) ? gradeMap.get(student.getGradeId()).getName() : "");
            exportDTO.setClassName(Objects.nonNull(classMap.get(student.getClassId())) ? classMap.get(student.getClassId()).getName() : "");
            exportDTO.setPlanStudentId(String.valueOf(student.getId()));
            exportDTO.setScreeningCode(Objects.isNull(student.getScreeningCode()) ? "" : String.valueOf(student.getScreeningCode()));
            exportList.add(exportDTO);
        });
        return exportList;
    }

    @Override
    public Class getHeadClass(ExportCondition exportCondition) {
        return QuestionnaireAccountExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return "问卷账号密码";
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return ExcelFileNameConstant.PLAN_STUDENT_FILE_NAME;
    }


    @Override
    public File generateExcelFile(String fileName, List data, ExportCondition exportCondition) throws IOException {

        List<QuestionnaireAccountExportDTO> exportList = data;
        String path = Paths.get(UUID.randomUUID().toString(), schoolService.getById(exportCondition.getSchoolId()).getName()).toString();
        Map<String, List<QuestionnaireAccountExportDTO>> stringListMap = exportList.stream().collect(Collectors.groupingBy(QuestionnaireAccountExportDTO::getGradeName));
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);
        String filepath = null;
        for (Map.Entry<String, List<QuestionnaireAccountExportDTO>> gradeEntry : stringListMap.entrySet()) {
            List<QuestionnaireAccountExportDTO> classExport = gradeEntry.getValue();
            Map<String, List<QuestionnaireAccountExportDTO>> classMap = classExport.stream().collect(Collectors.groupingBy(QuestionnaireAccountExportDTO::getClassName));
            for (Map.Entry<String, List<QuestionnaireAccountExportDTO>> classEntry : classMap.entrySet()) {
                filepath = ExcelUtil.exportListToExcelWithFolder(Paths.get(path, gradeEntry.getKey(), classEntry.getKey()).toString(), classEntry.getKey(), classEntry.getValue(), mergeStrategy, getHeadClass(exportCondition)).getAbsolutePath();
            }
        }
        return ZipUtil.zip(StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(filepath, StrUtil.SLASH), StrUtil.SLASH), StrUtil.SLASH));
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_EXCEL_QUESTIONNAIRE_ACCOUNT,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getSchoolId(),
                exportCondition.getPlanId(),
                exportCondition.getGradeId());
    }
}