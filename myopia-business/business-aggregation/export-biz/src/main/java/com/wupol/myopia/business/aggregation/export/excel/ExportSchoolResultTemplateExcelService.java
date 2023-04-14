package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.date.DateUtil;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SchoolResultTemplateExcel;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导出学校筛查模板
 */
@Service(ExportReportServiceNameConstant.EXPORT_SCHOOL_RESULT_TEMPLATE_EXCEL_SERVICE)
@Slf4j
public class ExportSchoolResultTemplateExcelService extends BaseExportExcelFileService {

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     *
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        School school = schoolService.getById(exportCondition.getSchoolId());
        return String.format(CommonConst.EXPORT_SCHOOL_RESULT_TEMPLATE_EXCEL_NAME, school.getName());
    }

    /**
     * 获取生成Excel的数据
     *
     * @param exportCondition 导出条件
     *
     * @return java.util.List
     **/
    @Override
    public List getExcelData(ExportCondition exportCondition) {
        // 查询数据
        List<ScreeningPlanSchoolStudent> planStudentList = screeningPlanSchoolStudentService.getByPlanIdAndSchoolId(exportCondition.getPlanId(), exportCondition.getSchoolId());
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(planStudentList, ScreeningPlanSchoolStudent::getGradeId);
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(planStudentList, ScreeningPlanSchoolStudent::getClassId);
        Map<Integer, VisionScreeningResult> resultMap = visionScreeningResultService.getMapByPlanStudentIds(planStudentList.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toList()));
        return planStudentList.stream().map(s -> {
            SchoolResultTemplateExcel templateExcel = new SchoolResultTemplateExcel();
            templateExcel.setPlanStudentId(s.getId().toString());
            templateExcel.setSno(s.getStudentNo());
            templateExcel.setStudentName(s.getStudentName());
            templateExcel.setCredentials(s.getIdCard());
            templateExcel.setGender(GenderEnum.getCnName(s.getGender()));
            templateExcel.setBitrhday(DateUtil.formatDate(s.getBirthday()));
            templateExcel.setGradeName(gradeMap.getOrDefault(s.getGradeId(), new SchoolGrade()).getName());
            templateExcel.setClassName(classMap.getOrDefault(s.getClassId(), new SchoolClass()).getName());
            templateExcel.setPassport(s.getPassport());
            VisionScreeningResult result = resultMap.get(s.getId());
            templateExcel.setGlassesType(formatEyeDate(EyeDataUtil.glassesTypeString(result)));
            templateExcel.setRightNakedVision(formatEyeDate(EyeDataUtil.visionRightDataToStr(result)));
            templateExcel.setLeftNakedVision(formatEyeDate(EyeDataUtil.visionLeftDataToStr(result)));
            templateExcel.setRightCorrection(formatEyeDate(EyeDataUtil.correctedRightDataToStr(result)));
            templateExcel.setLeftCorrection(formatEyeDate(EyeDataUtil.correctedLeftDataToStr(result)));
            templateExcel.setRightSph(formatEyeDate(EyeDataUtil.computerRightSph(result)));
            templateExcel.setRightCyl(formatEyeDate(EyeDataUtil.computerRightCyl(result)));
            templateExcel.setRightAxial(formatEyeDate(EyeDataUtil.computerRightAxial(result)));
            templateExcel.setLeftSph(formatEyeDate(EyeDataUtil.computerLeftSph(result)));
            templateExcel.setLeftCyl(formatEyeDate(EyeDataUtil.computerLeftCyl(result)));
            templateExcel.setLeftAxial(formatEyeDate(EyeDataUtil.computerLeftAxial(result)));
            templateExcel.setHeight(EyeDataUtil.heightToStr(result));
            templateExcel.setWeight(EyeDataUtil.weightToStr(result));
            return templateExcel;
        }).collect(Collectors.toList());
    }

    /**
     * 获取Excel表头类
     *
     * @return java.lang.Class
     **/
    @Override
    public Class getHeadClass(ExportCondition exportCondition) {
        return SchoolResultTemplateExcel.class;
    }

    /**
     * 获取通知的关键内容
     *
     * @param exportCondition 导出条件
     *
     * @return java.lang.String
     **/
    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        ScreeningPlan plan = screeningPlanService.getById(exportCondition.getPlanId());
        School school = schoolService.getById(exportCondition.getSchoolId());
        return String.format(CommonConst.SCHOOL_TEMPLATE_EXCEL_NOTICE_KEY, plan.getTitle(), school.getName());
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_EXCEL_ORG,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getDistrictId());
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        Integer schoolId = exportCondition.getSchoolId();
        Integer planId = exportCondition.getPlanId();
        if (ObjectsUtil.hasNull(schoolId, planId)) {
            throw new BusinessException("请求参数有误");
        }
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudentService.getByPlanIdAndSchoolId(exportCondition.getPlanId(), exportCondition.getSchoolId()))) {
            throw new BusinessException("数据为空");
        }
    }

    /**
     * 格式化值
     *
     * @param val 值
     *
     * @return String
     */
    private String formatEyeDate(String val) {
        if (StringUtils.isNotBlank(val) && StringUtils.equals(val, "--")) {
            return StringUtils.EMPTY;
        }
        return val;
    }
}
