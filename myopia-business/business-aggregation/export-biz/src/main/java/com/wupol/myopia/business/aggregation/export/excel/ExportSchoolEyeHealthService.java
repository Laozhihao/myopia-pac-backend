package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.dto.EyeHealthDataExportDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 眼健康中心数据
 *
 * @author Simple4H
 */
@Service(ExportExcelServiceNameConstant.EXPORT_SCHOOL_EYE_HEALTH_SERVICE)
public class ExportSchoolEyeHealthService extends BaseExportExcelFileService {

    @Resource
    private SchoolStudentService schoolStudentService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return PDFFileNameConstant.SCHOOL_EYE_HEALTH;
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_EXCEL_SCHOOL_EYE_HEALTH,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getSchoolId());
    }

    @Override
    public List getExcelData(ExportCondition exportCondition) {
        Integer schoolId = exportCondition.getSchoolId();
        List<SchoolStudent> schoolStudents = schoolStudentService.listBySchoolId(schoolId);
        List<Integer> studentIds = schoolStudents.stream().map(SchoolStudent::getStudentId).collect(Collectors.toList());

        // 获取年级
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(schoolStudents, SchoolStudent::getGradeId);

        // 获取班级
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(schoolStudents, SchoolStudent::getClassId);

        // 结果表
        TwoTuple<Map<Integer, VisionScreeningResult>, Map<Integer, StatConclusion>> resultStatMap = visionScreeningResultService.getStudentResultAndStatMap(studentIds);
        Map<Integer, VisionScreeningResult> resultMap = resultStatMap.getFirst();
        Map<Integer, StatConclusion> statConclusionMap = resultStatMap.getSecond();

        return schoolStudents.stream().map(getExportData(gradeMap, classMap, resultMap, statConclusionMap)).collect(Collectors.toList());
    }

    private static Function<SchoolStudent, EyeHealthDataExportDTO> getExportData(Map<Integer, SchoolGrade> gradeMap, Map<Integer, SchoolClass> classMap,
                                                                                 Map<Integer, VisionScreeningResult> resultMap, Map<Integer, StatConclusion> statConclusionMap) {
        return s -> {
            EyeHealthDataExportDTO exportDTO = new EyeHealthDataExportDTO();
            exportDTO.setSno(s.getSno());
            exportDTO.setName(s.getName());
            exportDTO.setGender(GenderEnum.getCnName(s.getGender()));
            exportDTO.setBirthday(DateFormatUtil.format(s.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE2));
            exportDTO.setGradeAndClass(gradeMap.get(s.getGradeId()).getName() + classMap.get(s.getClassId()).getName());

            VisionScreeningResult result = resultMap.get(s.getStudentId());
            StatConclusion statConclusion = statConclusionMap.get(s.getStudentId());
            generateResultInfo(s, exportDTO, result);
            generateStatInfo(exportDTO, statConclusion);
            return exportDTO;
        };
    }

    /**
     * 筛查结果信息
     *
     * @param schoolStudent 学校学生
     * @param exportDTO     导出数据
     * @param result        筛查结果
     */
    private static void generateResultInfo(SchoolStudent schoolStudent, EyeHealthDataExportDTO exportDTO, VisionScreeningResult result) {
        if (Objects.isNull(result)) {
            return;
        }
        exportDTO.setScreeningTime(DateFormatUtil.format(schoolStudent.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE2));
        exportDTO.setLowVision(EyeDataUtil.mergeEyeData(EyeDataUtil.visionRightDataToStr(result), EyeDataUtil.visionLeftDataToStr(result)));
        exportDTO.setSph(EyeDataUtil.mergeEyeData(EyeDataUtil.computerRightSph(result), EyeDataUtil.computerLeftSph(result)));
        exportDTO.setCyl(EyeDataUtil.mergeEyeData(EyeDataUtil.computerRightCyl(result), EyeDataUtil.computerLeftCyl(result)));
        exportDTO.setAxial(EyeDataUtil.mergeEyeData(EyeDataUtil.computerRightAxial(result), EyeDataUtil.computerLeftAxial(result)));
        exportDTO.setCorrectedVision(EyeDataUtil.mergeEyeData(EyeDataUtil.correctedRightDataToStr(result), EyeDataUtil.correctedLeftDataToStr(result)));
        exportDTO.setHeight(EyeDataUtil.height(result).toString());
    }

    /**
     * 统计结果信息
     *
     * @param exportDTO      导出数据
     * @param statConclusion 统计结果
     */
    private static void generateStatInfo(EyeHealthDataExportDTO exportDTO, StatConclusion statConclusion) {
        if (Objects.isNull(statConclusion)) {
            return;
        }
        exportDTO.setWearingGlasses(StringUtils.defaultIfBlank(GlassesTypeEnum.getDescByCode(statConclusion.getGlassesType()), "--"));
        boolean isKindergarten = SchoolAge.checkKindergarten(statConclusion.getSchoolAge());
        if (Objects.equals(statConclusion.getIsLowVision(), Boolean.TRUE)) {
            exportDTO.setLowVisionResult(isKindergarten ? "视力低常" : "视力低下");
        } else {
            exportDTO.setLowVisionResult("正常");
        }
        exportDTO.setRefractiveResult(EyeDataUtil.getRefractiveResultDesc(statConclusion, isKindergarten));

        exportDTO.setCorrectedVisionResult(VisionCorrection.get(statConclusion.getVisionCorrection()).desc);
        exportDTO.setWarningLevel(WarningLevel.getDesc(statConclusion.getWarningLevel()));
        exportDTO.setReview(Objects.equals(statConclusion.getIsReview(), Boolean.TRUE) ? "建议复查" : "无");
        exportDTO.setGlassesType(GlassesTypeEnum.getDescByCode(statConclusion.getGlassesType()));

        TwoTuple<String, String> deskChairSuggest = EyeDataUtil.getDeskChairSuggest(exportDTO.getHeight(), statConclusion.getSchoolAge());
        exportDTO.setDesk(deskChairSuggest.getFirst());
        exportDTO.setChair(deskChairSuggest.getSecond());
        if (Objects.equals(MyopiaLevelEnum.seatSuggest(statConclusion.getMyopiaWarningLevel()), Boolean.TRUE)) {
            exportDTO.setSeat("座位与黑板相距5-6米");
        }
        exportDTO.setIsBindMp(Objects.equals(statConclusion.getIsBindMp(), Boolean.TRUE) ? "是" : "否");
    }


    @Override
    public Class getHeadClass(ExportCondition exportCondition) {
        return EyeHealthDataExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return PDFFileNameConstant.SCHOOL_EYE_HEALTH;
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        Integer schoolId = exportCondition.getSchoolId();
        List<SchoolStudent> schoolStudents = schoolStudentService.listBySchoolId(schoolId);
        if (CollectionUtils.isEmpty(schoolStudents)) {
            throw new BusinessException("数据为空！");
        }
    }
}
