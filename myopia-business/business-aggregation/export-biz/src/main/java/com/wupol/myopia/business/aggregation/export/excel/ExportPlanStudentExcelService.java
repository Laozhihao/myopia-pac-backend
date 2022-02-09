package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.util.ZipUtil;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
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

    @Resource
    private SchoolClassService schoolClassService;

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

        // 年级
        List<Integer> gradeIds = planSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toList());
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(gradeIds);

        // 班级
        List<Integer> classIds = planSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getClassId).collect(Collectors.toList());
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(classIds);

        planSchoolStudents.forEach(student -> {
            PlanStudentExportDTO exportDTO = new PlanStudentExportDTO();
            exportDTO.setScreeningCode(Objects.isNull(student.getScreeningCode()) ? "" : String.valueOf(student.getScreeningCode()));
            exportDTO.setName(student.getStudentName());
            // 身份证导出时不显示
            exportDTO.setIdCard("");
            exportDTO.setGender(GenderEnum.getName(student.getGender()));
            exportDTO.setBirthday(DateFormatUtil.format(student.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE2));
            exportDTO.setNation(NationEnum.getName(student.getNation()));
            exportDTO.setGradeName(Objects.nonNull(gradeMap.get(student.getGradeId())) ? gradeMap.get(student.getGradeId()).getName() : "");
            exportDTO.setClassName(Objects.nonNull(classMap.get(student.getClassId())) ? classMap.get(student.getClassId()).getName() : "");
            exportDTO.setStudentNo(student.getStudentNo());
            exportDTO.setPhone(student.getParentPhone());
            exportDTO.setProvince(districtService.getDistrictName(student.getProvinceCode()));
            exportDTO.setCity(districtService.getDistrictName(student.getCityCode()));
            exportDTO.setArea(districtService.getDistrictName(student.getAreaCode()));
            exportDTO.setTown(districtService.getDistrictName(student.getTownCode()));
            exportDTO.setAddress(student.getAddress());
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
            gradeName = schoolGradeService.getById(gradeId).getName();
        }
        return String.format(ExcelNoticeKeyContentConstant.PLAN_STUDENT_EXCEL_NOTICE_KEY_CONTENT,
                plan.getTitle(),
                DateFormatUtil.format(plan.getStartTime(), DateFormatUtil.FORMAT_ONLY_DATE),
                DateFormatUtil.format(plan.getEndTime(), DateFormatUtil.FORMAT_ONLY_DATE),
                school.getName(),
                gradeName);
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return ExcelFileNameConstant.PLAN_STUDENT_FILE_NAME;
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
    }

    @Override
    public File generateExcelFile(String fileName, List data) throws IOException {

        List<PlanStudentExportDTO> exportList = data;
        ScreeningPlanSchoolStudent planSchoolStudent = screeningPlanSchoolStudentService.getOneByStudentName(exportList.get(0).getName());
        String path = UUID.randomUUID() + "/" + planSchoolStudent.getSchoolName();
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

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_EXCEL_PLAN_STUDENT,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getSchoolId(),
                exportCondition.getPlanId(),
                exportCondition.getGradeId());
    }


}
