package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.util.ZipUtil;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.PlanStudentExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 导出筛查学生
 *
 * @author Simple4H
 */
@Service("planStudentExcelService")
public class ExportPlanStudentExcelService extends BaseExportExcelFileService {

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private DistrictService districtService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Override
    public List getExcelData(ExportCondition exportCondition) {

        Integer planId = exportCondition.getPlanId();
        Integer schoolId = exportCondition.getSchoolId();
        Integer gradeId = exportCondition.getGradeId();
        List<ScreeningPlanSchoolStudent> planSchoolStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolIdAndGradeId(planId, schoolId, gradeId);
        if (CollectionUtils.isEmpty(planSchoolStudents)) {
            throw new BusinessException("数据为空");
        }
        List<PlanStudentExportDTO> exportList = new ArrayList<>(planSchoolStudents.size());

        planSchoolStudents.forEach(student -> {
            PlanStudentExportDTO exportDTO = new PlanStudentExportDTO();
            exportDTO.setScreeningCode(Objects.isNull(student.getScreeningCode()) ? "" : String.valueOf(student.getScreeningCode()));
            exportDTO.setName(student.getStudentName());
            exportDTO.setGender(GenderEnum.getName(student.getGender()));
            exportDTO.setBirthday(DateFormatUtil.format(student.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE));
            exportDTO.setSchoolName(student.getSchoolName());
            exportDTO.setGradeName(student.getGradeName());
            exportDTO.setClassName(student.getClassName());
            exportDTO.setStudentNo(student.getStudentNo());
            exportDTO.setPhone(student.getParentPhone());
            exportDTO.setAddress(districtService.getAddressDetails(student.getProvinceCode(), student.getCityCode(),
                    student.getAreaCode(), student.getTownCode(), student.getAddress()));
            exportList.add(exportDTO);
        });
        return exportList;
    }

    @Override
    public Class getHeadClass() {
        return PlanStudentExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        ScreeningPlan plan = screeningPlanService.getById(exportCondition.getPlanId());
        School school = schoolService.getById(exportCondition.getSchoolId());
        String gradeName = "";
        Integer gradeId = exportCondition.getGradeId();
        if (Objects.nonNull(gradeId)) {
            gradeName =  schoolGradeService.getById(gradeId).getName();
        }

        return String.format(ExcelFileNameConstant.PLAN_STUDENT_EXCEL_FILE_NAME,
                plan.getTitle(),
                DateFormatUtil.format(plan.getStartTime(), DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND),
                DateFormatUtil.format(plan.getEndTime(), DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND),
                school.getName(),
                gradeName);
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return ExcelFileNameConstant.PLAN_STUDENT_NAME;
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
    }

    @Override
    public File generateExcelFile(String fileName, List data) throws IOException {

        List<PlanStudentExportDTO> exportList = data;
        String path = UUID.randomUUID() + "/" + exportList.get(0).getSchoolName();
        Map<String, List<PlanStudentExportDTO>> stringListMap = exportList.stream().collect(Collectors.groupingBy(PlanStudentExportDTO::getGradeName));
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);
        String filepath = null;
        for (Map.Entry<String, List<PlanStudentExportDTO>> gradeEntry : stringListMap.entrySet()) {
            List<PlanStudentExportDTO> classExport = gradeEntry.getValue();
            Map<String, List<PlanStudentExportDTO>> classMap = classExport.stream().collect(Collectors.groupingBy(PlanStudentExportDTO::getClassName));
            for (Map.Entry<String, List<PlanStudentExportDTO>> classEntry : classMap.entrySet()) {
                filepath = ExcelUtil.exportListToExcelWithFolder(path + "/" + gradeEntry.getKey() + "/" + classEntry.getKey(), classEntry.getKey(), classEntry.getValue(), mergeStrategy, getHeadClass()).getAbsolutePath();
            }
        }
        return ZipUtil.zip(StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(filepath, "/"), "/"), "/"));
    }
}
