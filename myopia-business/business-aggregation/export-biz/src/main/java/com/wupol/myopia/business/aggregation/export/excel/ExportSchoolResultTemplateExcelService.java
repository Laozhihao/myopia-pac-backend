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
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
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
        return "导入学生筛查数据";
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
//        visionScreeningResultService.
        return planStudentList.stream().map(s -> {
            SchoolResultTemplateExcel templateExcel = new SchoolResultTemplateExcel();
            templateExcel.setPlanStudentId(s.getId().toString());
            templateExcel.setSno(s.getStudentNo());
            templateExcel.setStudentName(s.getStudentName());
            templateExcel.setCredentials(s.getIdCard());
            templateExcel.setGender(GenderEnum.getCnName(s.getGender()));
            templateExcel.setBitrhday(DateUtil.formatTime(s.getBirthday()));
            templateExcel.setGradeName(gradeMap.getOrDefault(s.getGradeId(), new SchoolGrade()).getName());
            templateExcel.setClassName(classMap.getOrDefault(s.getClassId(), new SchoolClass()).getName());
            templateExcel.setPassport(s.getPassport());
//            templateExcel.setGlassesType();
//            templateExcel.setRightNakedVision();
//            templateExcel.setLeftNakedVision();
//            templateExcel.setRightCorrection();
//            templateExcel.setLeftCorrection();
//            templateExcel.setRightSph();
//            templateExcel.setRightCyl();
//            templateExcel.setRightAxial();
//            templateExcel.setLeftSph();
//            templateExcel.setLeftCyl();
//            templateExcel.setLeftAxial();
//            templateExcel.setHeight();
//            templateExcel.setWeight();
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
}
