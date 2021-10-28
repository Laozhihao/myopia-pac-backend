package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.common.constant.ExportAddressKey;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningCountDTO;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 导出学生
 *
 * @author Simple4H
 */
@Service("studentExcelService")
@Log4j2
public class ExportStudentExcelService extends BaseExportExcelFileService {

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private DistrictService districtService;

    @Resource
    private StudentService studentService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private MedicalReportService medicalReportService;

    @Override
    public List getExcelData(ExportCondition exportCondition) {
        // 获取学校信息
        Integer schoolId = exportCondition.getSchoolId();
        School school = schoolService.getById(schoolId);
        String schoolName = school.getName();

        // 查询学生
        List<StudentDTO> studentLists = studentService.getBySchoolIdAndGradeIdAndClassId(schoolId, null, exportCondition.getGradeId());
        if (CollectionUtils.isEmpty(studentLists)) {
            return new ArrayList<>();
        }

        // 获取年级信息(同个学校的年级名称不存在相同的)
        List<Integer> gradeIdList = studentLists.stream().map(Student::getGradeId).distinct().collect(Collectors.toList());
        Map<Integer, String> gradeNameMap = schoolGradeService.getByIds(gradeIdList).stream().collect(Collectors.toMap(SchoolGrade::getId, SchoolGrade::getName));

        // 获取年级班级信息
        List<Integer> classIdList = studentLists.stream().map(StudentDTO::getClassId).distinct().collect(Collectors.toList());
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(classIdList);

        // 筛查次数
        List<StudentScreeningCountDTO> studentScreeningCountVOS = visionScreeningResultService.countScreeningTime();
        Map<Integer, Integer> countMaps = studentScreeningCountVOS.stream().collect(Collectors
                .toMap(StudentScreeningCountDTO::getStudentId,
                        StudentScreeningCountDTO::getCount));

        // 获取就诊记录
        List<Integer> studentIds = studentLists.stream().map(Student::getId).collect(Collectors.toList());
        List<ReportAndRecordDO> visitLists = medicalReportService.getByStudentIds(studentIds);
        Map<Integer, List<ReportAndRecordDO>> visitMap = visitLists.stream()
                .collect(Collectors.groupingBy(ReportAndRecordDO::getStudentId));

        List<StudentExportDTO> exportList = new ArrayList<>();
        for (StudentDTO item : studentLists) {
            HashMap<String, String> addressMap = generateAddressMap(item);
            StudentExportDTO exportVo = new StudentExportDTO()
                    .setNo(item.getSno())
                    .setName(item.getName())
                    .setSchoolNo(school.getSchoolNo())
                    .setGender(GenderEnum.getName(item.getGender()))
                    .setBirthday(DateFormatUtil.format(item.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE))
                    .setNation(NationEnum.getName(item.getNation()))
                    .setSchoolName(schoolName)
                    .setGrade(gradeNameMap.get(item.getGradeId()))
                    .setBindPhone(item.getMpParentPhone())
                    .setPhone(item.getParentPhone())
                    .setAddress(item.getAddress())
                    .setLabel(WarningLevel.getDesc(item.getVisionLabel()))
                    .setSituation(item.situation2Str())
                    .setScreeningCount(countMaps.getOrDefault(item.getId(), 0))
                    .setQuestionCount(0)
                    .setLastScreeningTime(DateFormatUtil.format(item.getLastScreeningTime(), DateFormatUtil.FORMAT_ONLY_DATE))
                    .setProvince(addressMap.getOrDefault(ExportAddressKey.PROVIDE, StringUtils.EMPTY))
                    .setCity(addressMap.getOrDefault(ExportAddressKey.CITY, StringUtils.EMPTY))
                    .setArea(addressMap.getOrDefault(ExportAddressKey.AREA, StringUtils.EMPTY))
                    .setTown(addressMap.getOrDefault(ExportAddressKey.TOWN, StringUtils.EMPTY));
            if (Objects.nonNull(visitMap.get(item.getId()))) {
                exportVo.setVisitsCount(visitMap.get(item.getId()).size());
            } else {
                exportVo.setVisitsCount(0);
            }
            if (Objects.nonNull(item.getClassId()) && null != classMap.get(item.getClassId())) {
                exportVo.setClassName(classMap.get(item.getClassId()).getName());
            }
            exportList.add(exportVo);
        }
        return exportList;
    }

    @Override
    public Class getHeadClass() {
        return StudentExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        School school = schoolService.getById(exportCondition.getSchoolId());
        String gradeName = Objects.nonNull(exportCondition.getGradeId()) ? schoolGradeService.getById(exportCondition.getGradeId()).getName() : StringUtils.EMPTY;
        // 行政区域
        District district = districtService.findOne(new District().setId(school.getDistrictId()));
        return String.format(ExcelNoticeKeyContentConstant.STUDENT_EXCEL_NOTICE_KEY_CONTENT,
                districtService.getTopDistrictName(district.getCode()),
                school.getName(),
                gradeName);
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        // 设置文件名
        StringBuilder builder = new StringBuilder().append(ExcelFileNameConstant.STUDENT_FILE_NAME);
        School school = schoolService.getById(exportCondition.getSchoolId());
        builder.append(school.getName());
        if (Objects.nonNull(exportCondition.getGradeId())) {
            String gradeName = schoolGradeService.getById(exportCondition.getGradeId()).getName();
            builder.append("-").append(gradeName);
        }
        return builder.toString();
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        // 不需要校验
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_EXCEL_STUDENT,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getSchoolId(),
                exportCondition.getGradeId());
    }
}
