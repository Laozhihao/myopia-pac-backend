package com.wupol.myopia.business.aggregation.export.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolStudentFacadeService;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentVisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/05/16:43
 * @Description: 导出学校学生筛查数据
 */
@Log4j2
@Service("exportPlanSchoolStudentDataExcelService")
public class ExportPlanSchoolStudentDataExcelService extends BaseExportExcelFileService{
    @Autowired
    private ResourceFileService resourceFileService;
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
    @Resource
    private ScreeningPlanSchoolStudentFacadeService screeningPlanSchoolStudentFacadeService;

    @Override
    public List getExcelData(ExportCondition exportCondition) {
        Integer screeningPlanId = exportCondition.getPlanId();
        Integer screeningOrgId = exportCondition.getScreeningOrgId();
        Integer schoolId = exportCondition.getSchoolId();
        Integer gradeId = exportCondition.getGradeId();
        Integer classId = exportCondition.getClassId();

        ScreeningStudentQueryDTO screeningStudentQueryDTO = new ScreeningStudentQueryDTO();

        screeningStudentQueryDTO.setScreeningPlanId(screeningPlanId);
        screeningStudentQueryDTO.setSchoolId(schoolId);
        screeningStudentQueryDTO.setGradeId(gradeId);
        screeningStudentQueryDTO.setClassId(classId);

        List<ScreeningStudentDTO> screeningStudentDTOS =  screeningPlanSchoolStudentService.selectListByQuery(screeningStudentQueryDTO);

        List<StudentVisionScreeningResultExportDTO> studentVisionScreeningResultExportDTOS = new ArrayList<>();
        screeningStudentDTOS.forEach(studentDTO -> {

            studentDTO.setNationDesc(NationEnum.getName(studentDTO.getNation()))
                    .setAddress(districtService.getAddressDetails(studentDTO.getProvinceCode(), studentDTO.getCityCode(), studentDTO.getAreaCode(), studentDTO.getTownCode(), studentDTO.getAddress()));

            //循环导出数据会慢，看是否有必要改成链表
            VisionScreeningResult visionScreeningResult = screeningPlanSchoolStudentFacadeService.getVisionScreeningResult(studentDTO);

            setStudentData(studentVisionScreeningResultExportDTOS, studentDTO, visionScreeningResult);
        });
        //对年级排序
        studentVisionScreeningResultExportDTOS.sort(Comparator.comparing((StudentVisionScreeningResultExportDTO planSchoolStudent) ->
                Integer.valueOf(GradeCodeEnum.getByName(planSchoolStudent.getGradeName()).getCode())));
        return studentVisionScreeningResultExportDTOS;
    }

    private void setStudentData(List<StudentVisionScreeningResultExportDTO> studentVisionScreeningResultExportDTOS, ScreeningStudentDTO studentDTO, VisionScreeningResult visionScreeningResult) {
        StudentVisionScreeningResultExportDTO studentVisionScreeningResultExportDTO = new StudentVisionScreeningResultExportDTO();
        studentVisionScreeningResultExportDTO.setId(studentDTO.getId());
        studentVisionScreeningResultExportDTO.setStudentName(EyeDataUtil.name(studentDTO));//姓名
        studentVisionScreeningResultExportDTO.setStudentNo(EyeDataUtil.sno(studentDTO));//学号
        studentVisionScreeningResultExportDTO.setGenderDesc(EyeDataUtil.gender(studentDTO));//性别

        studentVisionScreeningResultExportDTO.setGradeName(EyeDataUtil.gradeName(studentDTO));//性别
        studentVisionScreeningResultExportDTO.setClassName(EyeDataUtil.className(studentDTO));//性别
        studentVisionScreeningResultExportDTO.setBirthday(studentDTO.getBirthday());//性别

        studentVisionScreeningResultExportDTO.setParentPhone(EyeDataUtil.phone(studentDTO));//手机号码
        studentVisionScreeningResultExportDTO.setAddress(EyeDataUtil.address(studentDTO));//地址

        studentVisionScreeningResultExportDTO.setGlassesType(EyeDataUtil.glassesType(visionScreeningResult));//戴镜情况
        studentVisionScreeningResultExportDTO.setRightReScreenNakedVisions(EyeDataUtil.visionRightDataToStr(visionScreeningResult));//右眼裸视力
        studentVisionScreeningResultExportDTO.setRightReScreenCorrectedVisions(EyeDataUtil.correcteRightDataToStr(visionScreeningResult));//有眼矫正视力

        studentVisionScreeningResultExportDTO.setLeftReScreenNakedVisions(EyeDataUtil.visionLeftDataToStr(visionScreeningResult));//左眼裸视力
        studentVisionScreeningResultExportDTO.setLeftReScreenCorrectedVisions(EyeDataUtil.correcteLeftDataToStr(visionScreeningResult));//左眼矫正视力

        studentVisionScreeningResultExportDTO.setRightReScreenSphs(EyeDataUtil.computerRightSph(visionScreeningResult));//右眼球镜
        studentVisionScreeningResultExportDTO.setRightReScreenCyls(EyeDataUtil.computerRightCyl(visionScreeningResult));//右眼柱镜
        studentVisionScreeningResultExportDTO.setRightReScreenAxials(EyeDataUtil.computerRightAxial(visionScreeningResult));//右眼轴为
        studentVisionScreeningResultExportDTO.setRightReScreenSphericalEquivalents(EyeDataUtil.rightReScreenSph(visionScreeningResult));

        studentVisionScreeningResultExportDTO.setLeftReScreenSphs(EyeDataUtil.computerLeftSph(visionScreeningResult));//左眼球镜
        studentVisionScreeningResultExportDTO.setLeftReScreenCyls(EyeDataUtil.computerLeftCyl(visionScreeningResult));//左眼柱镜
        studentVisionScreeningResultExportDTO.setLeftReScreenAxials(EyeDataUtil.computerLeftAxial(visionScreeningResult));//左眼州为
        studentVisionScreeningResultExportDTO.setLeftReScreenSphericalEquivalents(EyeDataUtil.leftReScreenSph(visionScreeningResult));

        studentVisionScreeningResultExportDTOS.add(studentVisionScreeningResultExportDTO);
    }

    @Override
    public Class getHeadClass() {
        return StudentVisionScreeningResultExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        String noticeKeyContent = String.format(ExcelNoticeKeyContentConstant.EXPORT_PLAN_STUDENT_DATA, getFileNameTitle(exportCondition));
        return noticeKeyContent;
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

        String lockKey = String.format(RedisConstant.FILE_EXPORT_PLAN_STUDENTSCREENING, screeningPlanId,screeningOrgId,schoolId, gradeId,classId,userId);
        return lockKey;
    }

    @Override
    public String syncExport(ExportCondition exportCondition) {

        String parentPath = null;
        File excelFile = null;
        try {
            // 1.获取文件名
            String fileName = getFileName(exportCondition);
            // 3.获取数据，生成List
            List data = getExcelData(exportCondition);
            // 2.获取文件保存父目录路径
            excelFile = generateExcelFile(fileName, data);
            return resourceFileService.getResourcePath(s3Utils.uploadS3AndGetResourceFile(excelFile.getAbsolutePath(), fileName).getId());
        } catch (Exception e) {
            String requestData = JSON.toJSONString(exportCondition);
            log.error("【生成报告异常】{}", requestData, e);
            // 发送失败通知
            throw new BusinessException("导出数据异常");
        } finally {
            // 5.删除临时文件
            deleteTempFile(parentPath);
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
