package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentVisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/05/16:43
 * @Description: 导出学校学生筛查数据
 */
@Log4j2
@Service("exportVisionScreeningResultExcelService")
public class ExportVisionScreeningResultExcelService extends BaseExportExcelFileService{
    @Resource
    private SchoolService schoolService;
    @Resource
    private SchoolGradeService schoolGradeService;
    @Resource
    private SchoolClassService schoolClassService;
    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Resource
    private ScreeningPlanService screeningPlanService;

    @Override
    public List<StudentVisionScreeningResultExportDTO> getExcelData(ExportCondition exportCondition) {
        Integer screeningPlanId = exportCondition.getPlanId();
        Integer schoolId = exportCondition.getSchoolId();
        Integer gradeId = exportCondition.getGradeId();
        Integer classId = exportCondition.getClassId();

        ScreeningStudentQueryDTO screeningStudentQueryDTO = new ScreeningStudentQueryDTO();

        screeningStudentQueryDTO.setScreeningPlanId(screeningPlanId);
        screeningStudentQueryDTO.setSchoolId(schoolId);
        screeningStudentQueryDTO.setGradeId(gradeId);
        screeningStudentQueryDTO.setClassId(classId);

        List<ScreeningStudentDTO> screeningStudentDTOS =  screeningPlanSchoolStudentService.selectListByQuery(screeningStudentQueryDTO);

        List<VisionScreeningResult> resultList  = visionScreeningResultService.getByPlanStudentIds(screeningStudentDTOS.stream().map(ScreeningStudentDTO::getPlanStudentId).collect(Collectors.toList()));
        Map<Integer,List<VisionScreeningResult>> visionScreeningResultsGroup = resultList.stream().collect(Collectors.groupingBy(VisionScreeningResult::getStudentId));

        List<StudentVisionScreeningResultExportDTO> studentVisionScreeningResultExportDTOS = new ArrayList<>();
        screeningStudentDTOS.forEach(studentDTO -> {

            studentDTO.setNationDesc(NationEnum.getName(studentDTO.getNation()))
                    .setAddress(districtService.getAddressDetails(studentDTO.getProvinceCode(), studentDTO.getCityCode(), studentDTO.getAreaCode(), studentDTO.getTownCode(), studentDTO.getAddress()));

            VisionScreeningResult visionScreeningResult = EyeDataUtil.getVisionScreeningResult(studentDTO,visionScreeningResultsGroup);
            studentVisionScreeningResultExportDTOS.add(EyeDataUtil.setStudentData( studentDTO, visionScreeningResult)) ;
        });
        //对年级排序
        studentVisionScreeningResultExportDTOS.sort(Comparator.comparing((StudentVisionScreeningResultExportDTO planSchoolStudent) ->
                Integer.valueOf(GradeCodeEnum.getByName(planSchoolStudent.getGradeName()).getCode())));
        return studentVisionScreeningResultExportDTOS;
    }

    @Override
    public Class getHeadClass() {
        return StudentVisionScreeningResultExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return String.format(ExcelNoticeKeyContentConstant.EXPORT_PLAN_STUDENT_DATA, getFileNameTitle(exportCondition));
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return getFileNameTitle(exportCondition)+ ExcelFileNameConstant.PLAN_STUDENT_FILE_NAME;
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        Integer screeningPlanId = exportCondition.getPlanId();
        Integer screeningOrgId = exportCondition.getScreeningOrgId();
        Integer schoolId = exportCondition.getSchoolId();
        Integer gradeId = exportCondition.getGradeId();
        Integer classId = exportCondition.getClassId();
        Integer userId = exportCondition.getApplyExportFileUserId();

        return String.format(RedisConstant.FILE_EXPORT_PLAN_STUDENTSCREENING, screeningPlanId,screeningOrgId,schoolId, gradeId,classId,userId);
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition){
        ScreeningPlan screeningPlan = screeningPlanService.getById(exportCondition.getPlanId());
        if (Objects.isNull(screeningPlan)) {
            throw new BusinessException("筛查计划不存在");
        }
    }


    /**
     * 获取文件同步导出文件名称
     * @param exportCondition
     * @return
     */
    private String getFileNameTitle(ExportCondition exportCondition){
        School school = schoolService.getById(exportCondition.getSchoolId());

        String gradeName = "";
        Integer gradeId = exportCondition.getGradeId();
        if (Objects.nonNull(gradeId)) {
            gradeName = schoolGradeService.getById(gradeId).getName();
        }
        Integer classId = exportCondition.getClassId();
        String className = "";
        if (Objects.nonNull(classId)) {
            className = schoolClassService.getById(gradeId).getName();
        }
        return school.getName()+gradeName+className;
    }

}
