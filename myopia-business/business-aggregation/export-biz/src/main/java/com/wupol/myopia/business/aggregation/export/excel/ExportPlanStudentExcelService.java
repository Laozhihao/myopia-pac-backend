package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.PlanStudentExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List getExcelData(ExportCondition exportCondition) {

        Integer planId = exportCondition.getPlanId();
        Integer schoolId = exportCondition.getSchoolId();
        List<ScreeningPlanSchoolStudent> planSchoolStudents = screeningPlanSchoolStudentService.getByScreeningPlanIdAndSchoolId(planId, schoolId);
        List<PlanStudentExportDTO> exportList = new ArrayList<>(planSchoolStudents.size());

        planSchoolStudents.forEach(student -> {
            PlanStudentExportDTO exportDTO = new PlanStudentExportDTO();
            exportDTO.setScreeningCode(String.valueOf(student.getScreeningCode()));
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
        return String.format(ExcelFileNameConstant.PLAN_STUDENT_EXCEL_FILE_NAME, plan.getTitle(), plan.getStartTime(), plan.getEndTime(), school.getName());
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return ExcelFileNameConstant.PLAN_STUDENT_NAME;
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
    }
}
