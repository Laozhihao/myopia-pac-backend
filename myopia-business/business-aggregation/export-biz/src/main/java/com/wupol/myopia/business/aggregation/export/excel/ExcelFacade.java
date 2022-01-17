package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdcardUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.alibaba.fastjson.JSONPath;
import com.google.common.collect.Lists;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;

import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.base.util.ScreeningDataFormatUtils;

import com.wupol.myopia.base.util.*;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;

import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningResultPahtConst;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningDataContrastDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 统一处理 Excel 上传/下载
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
@Log4j2
@Service
public class ExcelFacade {

    @Resource
    private DistrictService districtService;

    @Resource
    private NoticeService noticeService;

    @Resource
    private S3Utils s3Utils;

    @Resource
    private RedisUtil redisUtil;


    @Autowired
    private SchoolStudentService schoolStudentService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SchoolGradeService schoolGradeService;

    /**
     * 导入学生
     *
     * @param createUserId  创建人userID
     * @param multipartFile 导入文件
     * @throws BusinessException 异常
     */
    public void importStudent(Integer createUserId, MultipartFile multipartFile, Integer schoolId) throws ParseException {
        List<Map<Integer, String>> listMap = readExcel(multipartFile);
        if (CollectionUtils.isEmpty(listMap)) {
            return;
        }

        // 判断是否导入到同一个学校(同个学校时没有"学校编号"列，第5列，index=4)
        boolean isSameSchool = Objects.nonNull(schoolId);
        int offset = isSameSchool ? 1 : 0;

        // 收集学校编号
        List<School> schools;
        String schoolNo = null;
        if (isSameSchool) {
            School school = schoolService.getById(schoolId);
            schoolNo = school.getSchoolNo();
            schools = Collections.singletonList(schoolService.getById(schoolId));
        } else {
            List<String> schoolNos = listMap.stream().map(s -> s.get(4)).collect(Collectors.toList());
            schools = schoolService.getBySchoolNos(schoolNos);
        }
        Map<String, Integer> schoolMap = schools.stream().collect(Collectors.toMap(School::getSchoolNo, School::getId));

        // 收集身份证号码
        List<String> idCards = listMap.stream().map(s -> s.get(8 - offset)).filter(Objects::nonNull).collect(Collectors.toList());

        // 数据预校验
        preCheckStudent(schools, idCards);

        // 收集年级信息
        List<SchoolGradeExportDTO> grades = schoolGradeService.getBySchoolIds(schools.stream().map(School::getId).collect(Collectors.toList()));
        schoolGradeService.packageGradeInfo(grades);

        // 通过学校编号分组
        Map<String, List<SchoolGradeExportDTO>> schoolGradeMaps = grades.stream().collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolNo));

        // 通过身份证获取学生
        Map<String, Student> studentMap = studentService.getByIdCardsAndStatus(idCards).stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));

        List<Student> importList = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            Student student = new Student();
            if (StringUtils.isBlank(item.get(0))) {
                break;
            }
            checkStudentInfo(item, offset);
            // Excel 格式： 姓名	性别	出生日期	民族   学校编号(同个学校时没有该列，后面的左移一列)   年级	班级	学号	身份证号	手机号码	省	市	县区	镇/街道	详细
            // 民族取值：1-汉族  2-蒙古族  3-藏族  4-壮族  5-回族  6-其他
            String idCard = item.get(8 - offset);
            if (Objects.nonNull(studentMap.get(idCard))) {
                throw new BusinessException("身份证" + idCard + "在系统中重复");
            }
            student.setName(item.get(0)).setGender(GenderEnum.getType(item.get(1))).setBirthday(DateFormatUtil.parseDate(item.get(2), DateFormatUtil.FORMAT_ONLY_DATE2)).setNation(NationEnum.getCode(item.get(3))).setGradeType(GradeCodeEnum.getByName(item.get(5 - offset)).getType()).setSno((item.get(7 - offset))).setIdCard(idCard).setParentPhone(item.get(9 - offset)).setCreateUserId(createUserId);
            student.setProvinceCode(districtService.getCodeByName(item.get(10 - offset)));
            student.setCityCode(districtService.getCodeByName(item.get(11 - offset)));
            student.setAreaCode(districtService.getCodeByName(item.get(12 - offset)));
            student.setTownCode(districtService.getCodeByName(item.get(13 - offset)));
            student.setAddress(item.get(14 - offset));
            // 通过学校编号获取改学校的年级信息
            List<SchoolGradeExportDTO> schoolGradeExportVOS = schoolGradeMaps.get(isSameSchool ? schoolNo : item.get(4));
            // 转换成年级Maps，年级名称作为Key
            Map<String, SchoolGradeExportDTO> gradeMaps = schoolGradeExportVOS.stream().collect(Collectors.toMap(SchoolGradeExportDTO::getName, Function.identity()));
            // 年级信息
            SchoolGradeExportDTO schoolGradeExportDTO = gradeMaps.get(item.get(5 - offset));
            Assert.notNull(schoolGradeExportDTO, "年级数据异常");
            // 设置年级ID
            student.setGradeId(schoolGradeExportDTO.getId());
            // 获取年级内的班级信息
            List<SchoolClassExportDTO> classExportVOS = schoolGradeExportDTO.getChild();
            // 转换成班级Maps 把班级名称作为key
            Map<String, Integer> classExportMaps = classExportVOS.stream().collect(Collectors.toMap(SchoolClassExportDTO::getName, SchoolClassExportDTO::getId));
            Integer classId = classExportMaps.get(item.get(6 - offset));
            Assert.notNull(classId, "班级数据为空");
            // 设置班级信息
            student.setClassId(classId);
            if (isSameSchool) {
                student.setSchoolId(schoolId);
            } else {
                student.setSchoolId(schoolMap.get((item.get(4 - offset))));
            }
            importList.add(student);
        }
        // 通过身份证获取已经删除的学生
        List<Student> deleteStudent = studentService.getDeleteStudentByIdCard(idCards);
        Map<String, Integer> deletedMap = deleteStudent.stream().collect(Collectors.toMap(Student::getIdCard, Student::getId));
        importList.forEach(student -> {
            if (Objects.nonNull(deletedMap.get(student.getIdCard()))) {
                student.setId(deletedMap.get(student.getIdCard()));
                student.setStatus(CommonConst.STATUS_NOT_DELETED);
            }

        });
        studentService.saveOrUpdateBatch(importList);
    }


    /**
     * 读取Excel数据
     *
     * @param multipartFile Excel文件
     * @return java.util.List<java.util.Map < java.lang.Integer, java.lang.String>>
     **/
    public List<Map<Integer, String>> readExcel(MultipartFile multipartFile) {
        String fileName = IOUtils.getTempPath() + multipartFile.getName() + "_" + System.currentTimeMillis() + CommonConst.FILE_SUFFIX;
        File file = new File(fileName);
        try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        } catch (IOException e) {
            log.error("导入学生数据异常:", e);
            throw new BusinessException("导入学生数据异常");
        }
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        try {
            List<Map<Integer, String>> listMap = EasyExcel.read(fileName).sheet().doReadSync();
            if (!CollectionUtils.isEmpty(listMap)) {
                listMap.remove(0);
            }
            return listMap;
        } catch (Exception e) {
            log.error("导入学生数据异常:", e);
            throw new BusinessException("Excel解析异常");
        }
    }

    /**
     * 导出统计报表 - 数据对比表
     *
     * @param userId     用户ID
     * @param exportList 导出数据
     * @param template   导出模板
     * @throws IOException   IO异常
     * @throws UtilException 工具异常
     */
    public void exportStatContrast(Integer userId, List<ScreeningDataContrastDTO> exportList, InputStream template) throws IOException, UtilException {
        String fileName = "统计对比报表";
        log.info("导出统计对比报文件: {}", fileName);
        File file = ExcelUtil.exportHorizonListToExcel(fileName, exportList, template);
        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS, fileName, new Date());
        noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * 导出筛查数据
     *
     * @param userId                   用户Id
     * @param statConclusionExportDTOs 导出筛查学生
     * @param isSchoolExport           是否学校维度导出
     * @param districtOrSchoolName     行政区域或学校名称
     * @param redisKey                 缓存值
     * @throws IOException   IO异常
     * @throws UtilException 工具异常
     */
    @Async
    public void generateVisionScreeningResult(Integer userId, List<StatConclusionExportDTO> statConclusionExportDTOs, boolean isSchoolExport, String districtOrSchoolName, String redisKey) throws IOException, UtilException {
        // 设置导出的文件名
        String fileName = String.format("%s-筛查数据", districtOrSchoolName);
        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS, districtOrSchoolName + "筛查数据", new Date());
        log.info("导出筛查结果文件: {}", fileName);
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);
        if (isSchoolExport) {
            List<VisionScreeningResultExportDTO> visionScreeningResultExportVos = genVisionScreeningResultExportVos(statConclusionExportDTOs);
            visionScreeningResultExportVos.sort(Comparator.comparing((VisionScreeningResultExportDTO exportDTO) -> Integer.valueOf(GradeCodeEnum.getByName(exportDTO.getGradeName()).getCode())));
            File excelFile = ExcelUtil.exportListToExcel(fileName, visionScreeningResultExportVos, mergeStrategy, VisionScreeningResultExportDTO.class);
            noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(excelFile), CommonConst.NOTICE_STATION_LETTER);
        } else {
            String folder = String.format("%s-%s", System.currentTimeMillis(), UUID.randomUUID());
            Map<String, List<StatConclusionExportDTO>> schoolNameMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getSchoolName));
            schoolNameMap.keySet().forEach(schoolName -> {
                List<VisionScreeningResultExportDTO> visionScreeningResultExportVos = genVisionScreeningResultExportVos(schoolNameMap.getOrDefault(schoolName, Collections.emptyList()));
                visionScreeningResultExportVos.sort(Comparator.comparing((VisionScreeningResultExportDTO exportDTO) -> Integer.valueOf(GradeCodeEnum.getByName(exportDTO.getGradeName()).getCode())));
                String excelFileName = String.format("%s-筛查数据", schoolName);
                try {
                    ExcelUtil.exportListToExcelWithFolder(folder, excelFileName, visionScreeningResultExportVos, mergeStrategy, VisionScreeningResultExportDTO.class);
                } catch (Exception e) {
                    redisUtil.del(redisKey);
                    log.error(e);
                }
            });
            File zipFile = ExcelUtil.zip(folder, fileName);
            noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(zipFile), CommonConst.NOTICE_STATION_LETTER);
        }
        redisUtil.del(redisKey);
    }

    /**
     * 生成筛查数据
     *
     * @param statConclusionExportDTOs 处理后筛查数据
     * @return List<VisionScreeningResultExportDTO>
     */
    public List<VisionScreeningResultExportDTO> genVisionScreeningResultExportVos(List<StatConclusionExportDTO> statConclusionExportDTOs) {
        Map<Boolean, List<StatConclusionExportDTO>> isRescreenMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getIsRescreen));
        Map<Integer, StatConclusionExportDTO> rescreenPlanStudentIdVoMap = isRescreenMap.getOrDefault(true, Collections.emptyList()).stream().collect(Collectors.toMap(StatConclusionExportDTO::getScreeningPlanSchoolStudentId, Function.identity(), (x, y) -> x));
        List<VisionScreeningResultExportDTO> exportVos = new ArrayList<>();
        List<StatConclusionExportDTO> vos = isRescreenMap.getOrDefault(false, Collections.emptyList());
        for (StatConclusionExportDTO vo : vos) {
            VisionScreeningResultExportDTO exportVo = new VisionScreeningResultExportDTO();
            BeanUtils.copyProperties(vo, exportVo);
            exportVo.setGenderDesc(GenderEnum.getName(vo.getGender()))
                    .setNationDesc(StringUtils.defaultString(NationEnum.getName(vo.getNation())))
                    .setGlassesTypeDesc(StringUtils.defaultIfBlank(GlassesTypeEnum.getDescByCode(vo.getGlassesType()), "--"))
                    .setIsRescreenDesc("否").setWarningLevelDesc(StringUtils.defaultIfBlank(WarningLevel.getDesc(vo.getWarningLevel()), "--"))
                    .setParentPhone(vo.getParentPhone())
                    .setAddress(districtService.getAddressDetails(vo.getProvinceCode(), vo.getCityCode(), vo.getAreaCode(), vo.getTownCode(), vo.getAddress()));
            genScreeningData(vo, exportVo);
            genReScreeningData(rescreenPlanStudentIdVoMap, vo, exportVo);
            generateDate(vo, exportVo);
            exportVos.add(exportVo);
        }
        return exportVos;
    }

    /**
     * 生成Excel数据
     *
     * @param dto       处理后筛查数据
     * @param exportDTO 筛查数据导出
     */
    private void generateDate(StatConclusionExportDTO dto, VisionScreeningResultExportDTO exportDTO) {
        exportDTO.setOcularInspectionSotropia(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OID_ESOTROPIA)));
        exportDTO.setOcularInspectionXotropia(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OID_EXOTROPIA)));
        exportDTO.setOcularInspectionVerticalStrabismus(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OID_VERTICAL_STRABISMUS)));
        exportDTO.setOcularInspectionDiagnosis(ScreeningDataFormatUtils.singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OID_DIAGNOSIS)));

        exportDTO.setVisionDiagnosis(ScreeningDataFormatUtils.singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_VD_DIAGNOSIS)));

        exportDTO.setComputerOptometryDiagnosis(ScreeningDataFormatUtils.singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_CO_diagnosis)));

        exportDTO.setSlitLampLeftEye(ListUtil.objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SLD_LEFT_PATHOLOGICAL_TISSUES)));
        exportDTO.setSlitLampLeftResult(ScreeningDataFormatUtils.singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SLD_LEFT_DIAGNOSIS)));
        exportDTO.setSlitLampRightEye(ListUtil.objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SLD_RIGHT_PATHOLOGICAL_TISSUES)));
        exportDTO.setSlitLampRightResult(ScreeningDataFormatUtils.singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SLD_RIGHT_DIAGNOSIS)));

        exportDTO.setLeftPupilOptometrySph(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_SPN)));
        exportDTO.setRightPupilOptometrySph(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_SPN)));
        exportDTO.setLeftPupilOptometryCyl(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_CYL)));
        exportDTO.setRightPupilOptometryCyl(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_CYL)));
        exportDTO.setLeftPupilOptometryAxial(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_AXIAL)));
        exportDTO.setRightPupilOptometryAxial(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_AXIAL)));
        exportDTO.setLeftPupilOptometryCorrectedVision(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_CORRECTEDVISION)));
        exportDTO.setRightPupilOptometryCorrectedVision(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_CORRECTEDVISION)));
        exportDTO.setPupilOptometryDiagnosis(ScreeningDataFormatUtils.singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_DIAGNOSIS)));
        exportDTO.setPupilOptometryResult(StatUtil.getRefractiveResult((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_SPN), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_CYL), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_SPN), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_CYL), DateUtil.ageOfNow(dto.getBirthday()), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION)));

        exportDTO.setLeftBiometricK1(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K1)));
        exportDTO.setLeftBiometricK1Axis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K1_AXIS)));
        exportDTO.setRightBiometricK1(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K1)));
        exportDTO.setRightBiometricK1Axis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K1_AXIS)));
        exportDTO.setLeftBiometricK2(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K2)));
        exportDTO.setLeftBiometricK2Axis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K2_AXIS)));
        exportDTO.setRightBiometricK2(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K2)));
        exportDTO.setRightBiometricK2Axis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K2_AXIS)));
        exportDTO.setLeftBiometricAST(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_AST)));
        exportDTO.setLeftBiometricASTAxis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K1_AXIS)));
        exportDTO.setRightBiometricAST(ScreeningDataFormatUtils.genEyeBiometric(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_AST)));
        exportDTO.setRightBiometricASTAxis(ScreeningDataFormatUtils.genBiometricAxis(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K1_AXIS)));
        exportDTO.setLeftBiometricPD(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_PD)));
        exportDTO.setRightBiometricPD(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_PD)));
        exportDTO.setLeftBiometricWTW(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_WTW)));
        exportDTO.setRightBiometricWTW(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_WTW)));
        exportDTO.setLeftBiometricAL(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_AL)));
        exportDTO.setRightBiometricAL(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_AL)));
        exportDTO.setLeftBiometricCCT(ScreeningDataFormatUtils.generateSingleSuffixUMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_CCT)));
        exportDTO.setRightBiometricCCT(ScreeningDataFormatUtils.generateSingleSuffixUMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_CCT)));
        exportDTO.setLeftBiometricAD(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_AD)));
        exportDTO.setRightBiometricAD(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_AD)));
        exportDTO.setLeftBiometricLT(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_LT)));
        exportDTO.setRightBiometricLT(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_LT)));
        exportDTO.setLeftBiometricVT(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_VT)));
        exportDTO.setRightBiometricVT(ScreeningDataFormatUtils.generateSingleSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_VT)));

        exportDTO.setLeftEyePressureDate(ScreeningDataFormatUtils.ipDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_IPD_LEFT_PRESSURE)));
        exportDTO.setRightEyePressureDate(ScreeningDataFormatUtils.ipDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_IPD_RIGHT_PRESSURE)));
        exportDTO.setLeftFundusData(ScreeningDataFormatUtils.singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_DF_LEFT_HASABNORMAL)));
        exportDTO.setRightFundusData(ScreeningDataFormatUtils.singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_DF_RIGHT_HASABNORMAL)));

        exportDTO.setOtherEyeDiseasesLeftEyeDiseases(ListUtil.objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OED_LEFT_EYE_DISEASES)));
        exportDTO.setOtherEyeDiseasesRightEyeDiseases(ListUtil.objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OED_RIGHT_EYE_DISEASES)));
        exportDTO.setOtherEyeDiseasesSystemicDiseaseSymptom((String) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SYSTEMIC_DISEASE_SYMPTOM));
        exportDTO.setLeftOtherEyeDiseasesLevel(ScreeningDataFormatUtils.levelDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_VLLD_LEFT_LEVEL)));
        exportDTO.setRightOtherEyeDiseasesLevel(ScreeningDataFormatUtils.levelDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_VLLD_RIGHT_LEVEL)));
    }

    /**
     * 组装复筛数据
     *
     * @param rescreenPlanStudentIdDTOMap 复筛学生信息
     * @param dto                         处理后筛查数据
     * @param exportDTO                   筛查数据导出
     */
    private void genReScreeningData(Map<Integer, StatConclusionExportDTO> rescreenPlanStudentIdDTOMap, StatConclusionExportDTO dto, VisionScreeningResultExportDTO exportDTO) {
        StatConclusionExportDTO rescreenVo = rescreenPlanStudentIdDTOMap.get(dto.getScreeningPlanSchoolStudentId());
        if (Objects.nonNull(rescreenVo)) {
            exportDTO.setLeftReScreenNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION)));
            exportDTO.setRightReScreenNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION)));
            exportDTO.setLeftReScreenCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_CORRECTED_VISION)));
            exportDTO.setRightReScreenCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_CORRECTED_VISION)));
            exportDTO.setLeftReScreenSphs(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_SPH)));
            exportDTO.setRightReScreenSphs(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_SPH)));
            exportDTO.setLeftReScreenCyls(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_CYL)));
            exportDTO.setRightReScreenCyls(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_CYL)));
            exportDTO.setLeftReScreenAxials(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_AXIAL)));
            exportDTO.setRightReScreenAxials(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_AXIAL)));
            exportDTO.setLeftReScreenSphericalEquivalents(ScreeningDataFormatUtils.singleEyeSEFormat(StatUtil.getSphericalEquivalent((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_SPH), (BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_CYL))));
            exportDTO.setRightReScreenSphericalEquivalents(ScreeningDataFormatUtils.singleEyeSEFormat(StatUtil.getSphericalEquivalent((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_SPH), (BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_CYL))));
            exportDTO.setIsRescreenDesc("是");
        }
    }

    /**
     * 组装初筛数据
     *
     * @param dto       处理后筛查数据
     * @param exportDTO 筛查数据导出
     */
    private void genScreeningData(StatConclusionExportDTO dto, VisionScreeningResultExportDTO exportDTO) {
        exportDTO.setLeftNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION))).setRightNakedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION))).setLeftCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CORRECTED_VISION))).setRightCorrectedVisions(ScreeningDataFormatUtils.singleEyeDateFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CORRECTED_VISION))).setRightSphs(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_SPH))).setLeftSphs(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_SPH))).setRightCyls(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CYL))).setLeftCyls(ScreeningDataFormatUtils.generateSingleSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CYL))).setRightAxials(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_AXIAL))).setLeftAxials(ScreeningDataFormatUtils.generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_AXIAL)));
        exportDTO.setComputerOptometryResult(StatUtil.getRefractiveResult((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_SPH), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CYL), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_SPH), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CYL), DateUtil.ageOfNow(dto.getBirthday()), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION)));
    }



    /**
     * 角膜曲率（单眼）
     *
     * @param val1 值1
     * @return String
     */
    private String genEyeBiometric(Object val1) {
        return Objects.nonNull(val1) ? StringUtils.isNotBlank(String.valueOf(val1)) ? val1 + "D" : "--" : "--";
    }

    /**
     * 角膜曲率（单眼）
     *
     * @param val1 值1
     * @return String
     */
    private String genBiometricAxis(Object val1) {
        return Objects.nonNull(val1) ? StringUtils.isNotBlank(String.valueOf(val1)) ? val1 + "°" : "--" : "--";
    }


    /**
     * 单眼数据格式化
     *
     * @param date 左眼数据
     * @return String
     */
    private String singleEyeDateFormat(BigDecimal date) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return Objects.isNull(date) ? "--" : decimalFormat.format(date);
    }

    /**
     * 单眼数据格式化
     *
     * @param date 左眼数据
     * @return String
     */
    private String singleEyeSEFormat(BigDecimal date) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        if (Objects.isNull(date)) {
            return "--";
        }
        String formatVal = decimalFormat.format(date);
        if (StringUtils.isNotBlank(formatVal) && BigDecimalUtil.moreThanAndEqual(formatVal, "0")) {
            return "+" + formatVal;
        }
        return formatVal;
    }

    /**
     * 格式化眼压数据
     *
     * @param data 眼数据
     * @return String
     */
    private String ipDateFormat(Object data) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return Objects.isNull(data) ? "--" : decimalFormat.format(data) + "mmHg";
    }

    /**
     * 格式化等级数据
     *
     * @param data 眼数据
     * @return String
     */
    private String levelDateFormat(Object data) {
        return Objects.isNull(data) ? "--" : data + "级";
    }

    /**
     * 检查学生信息是否完整
     *
     * @param item   学生信息
     * @param offset 偏移量(导入的为同一个学校的数据时，没有学校编号列，后面的左移一列)
     */
    private void checkStudentInfo(Map<Integer, String> item, int offset) {
        Assert.isTrue(StringUtils.isNotBlank(item.get(1)) && !GenderEnum.getType(item.get(1)).equals(GenderEnum.UNKNOWN.type), "学生性别异常");
        Assert.isTrue(StringUtils.isNotBlank(item.get(2)), "学生出生日期不能为空");
        if (offset > 0) {
            Assert.isTrue(StringUtils.isNotBlank(item.get(4)), "学校编号不能为空");
        }
        Assert.isTrue(StringUtils.isNotBlank(item.get(5 - offset)), "学生年级不能为空");
        Assert.isTrue(StringUtils.isNotBlank(item.get(6 - offset)), "学生班级不能为空");
        Assert.isTrue(StringUtils.isNotBlank(item.get(8 - offset)) && Pattern.matches(RegularUtils.REGULAR_ID_CARD, item.get(8 - offset)), "学生身份证" + item.get(8 - offset) + "异常");
        Assert.isTrue(StringUtils.isBlank(item.get(9 - offset)) || Pattern.matches(RegularUtils.REGULAR_MOBILE, item.get(9 - offset)), "学生手机号码" + item.get(9 - offset) + "异常");
    }

    /**
     * 初步结果 单眼
     *
     * @param diagnosis 0-正常 1-"（疑似）异常"
     * @return String
     */
    private String singleDiagnosis2String(Integer diagnosis) {
        if (Objects.isNull(diagnosis)) {
            return StringUtils.EMPTY;
        }
        if (0 == diagnosis) {
            return "正常";
        }
        if (1 == diagnosis) {
            return "（疑似）异常";
        }
        return StringUtils.EMPTY;
    }

    /**
     * List转换成String
     *
     * @param obj List
     * @return String
     */
    private String objectList2Str(Object obj) {
        List<String> result = new ArrayList<>();
        if (obj instanceof ArrayList<?>) {
            for (Object o : (List<?>) obj) {
                result.add((String) o);
            }
        }
        return String.join(",", result);
    }

    /**
     * 生成单眼度数String，后缀为°
     *
     * @param val 值
     * @return String
     */
    private String generateSingleEyeDegree(Object val) {
        return Objects.nonNull(val) ? val + "°" : "--";
    }

    /**
     * 单眼后缀为D
     *
     * @param val 值
     * @return String
     */
    private String generateSingleSuffixDStr(Object val) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if (Objects.nonNull(val)) {
            String formatVal = decimalFormat.format(val);
            if (StringUtils.isNotBlank(formatVal) && BigDecimalUtil.moreThanAndEqual(formatVal, "0")) {
                return "+" + formatVal + "D";
            }
            return formatVal + "D";
        }
        return "--";
    }

    /**
     * 单眼后缀为mm
     *
     * @param val 值
     * @return String
     */
    private String generateSingleSuffixMMStr(Object val) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return (StringUtils.isNotBlank((CharSequence) val) ? decimalFormat.format(new BigDecimal((String) val)) + "mm" : "--");
    }

    /**
     * 单眼后缀为um
     *
     * @param val 值
     * @return String
     */
    private String generateSingleSuffixUMStr(Object val) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return (StringUtils.isNotBlank((CharSequence) val) ? decimalFormat.format(new BigDecimal((String) val)) + "um" : "--");
    }

    /**
     * 导入学校学生
     *
     * @param createUserId  创建人
     * @param multipartFile 文件
     * @param schoolId      学校Id
     * @throws ParseException 转换异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void importSchoolStudent(Integer createUserId, MultipartFile multipartFile, Integer schoolId) throws ParseException {
        List<Map<Integer, String>> listMap = readExcel(multipartFile);
        if (CollectionUtils.isEmpty(listMap)) {
            return;
        }

        School school = schoolService.getById(schoolId);

        // 收集身份证号码、学号
        List<String> idCards = listMap.stream().map(s -> s.get(7)).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> snos = listMap.stream().map(s -> s.get(6)).filter(Objects::nonNull).collect(Collectors.toList());
        checkIdCard(idCards, snos);

        // 获取学校学生
        List<SchoolStudent> studentList = schoolStudentService.getByIdCardOrSno(idCards, snos, schoolId);
        Map<String, SchoolStudent> snoMap = studentList.stream().collect(Collectors.toMap(SchoolStudent::getSno, Function.identity()));
        Map<String, SchoolStudent> idCardMap = studentList.stream().collect(Collectors.toMap(SchoolStudent::getIdCard, Function.identity()));

        List<SchoolStudent> deletedSchoolStudents = schoolStudentService.getDeletedByIdCard(idCards, schoolId);
        Map<String, SchoolStudent> deletedStudentMap = deletedSchoolStudents.stream().collect(Collectors.toMap(SchoolStudent::getIdCard, Function.identity()));

        // 收集年级信息
        List<SchoolGradeExportDTO> grades = schoolGradeService.getBySchoolIds(Lists.newArrayList(school.getId()));
        schoolGradeService.packageGradeInfo(grades);

        // 年级信息通过学校Id分组
        Map<Integer, List<SchoolGradeExportDTO>> schoolGradeMaps = grades.stream().collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolId));

        for (Map<Integer, String> item : listMap) {
            SchoolStudent schoolStudent;
            SchoolStudent deletedSchoolStudent = deletedStudentMap.get(item.get(7));
            if (Objects.isNull(deletedSchoolStudent)) {
                schoolStudent = new SchoolStudent();
            } else {
                schoolStudent = deletedSchoolStudent;
            }
            if (StringUtils.isBlank(item.get(0))) {
                break;
            }
            checkIsExist(snoMap, idCardMap, item.get(6), item.get(7), item.get(1), item.get(2), item.get(4));
            schoolStudent.setName(item.get(0))
                    .setGender(GenderEnum.getType(item.get(1)))
                    .setBirthday(DateFormatUtil.parseDate(item.get(2), DateFormatUtil.FORMAT_ONLY_DATE2))
                    .setNation(NationEnum.getCode(item.get(3)))
                    .setSchoolNo(school.getSchoolNo())
                    .setGradeType(GradeCodeEnum.getByName(item.get(4)).getType())
                    .setSno((item.get(6)))
                    .setIdCard(item.get(7))
                    .setParentPhone(item.get(8))
                    .setCreateUserId(createUserId)
                    .setSchoolId(schoolId)
                    .setStatus(CommonConst.STATUS_NOT_DELETED);
            schoolStudent.setProvinceCode(districtService.getCodeByName(item.get(9)));
            schoolStudent.setCityCode(districtService.getCodeByName(item.get(10)));
            schoolStudent.setAreaCode(districtService.getCodeByName(item.get(11)));
            schoolStudent.setTownCode(districtService.getCodeByName(item.get(12)));
            schoolStudent.setAddress(item.get(13));
            // 通过学校编号获取改学校的年级信息
            List<SchoolGradeExportDTO> schoolGradeExportVOS = schoolGradeMaps.get(schoolId);
            // 转换成年级Maps，年级名称作为Key
            Map<String, SchoolGradeExportDTO> gradeMaps = schoolGradeExportVOS.stream().collect(Collectors.toMap(SchoolGradeExportDTO::getName, Function.identity()));
            // 年级信息
            SchoolGradeExportDTO schoolGradeExportDTO = gradeMaps.get(item.get(4));
            Assert.notNull(schoolGradeExportDTO, "年级数据异常");
            // 设置年级ID
            schoolStudent.setGradeId(schoolGradeExportDTO.getId());
            schoolStudent.setGradeName(item.get(4));
            // 获取年级内的班级信息
            List<SchoolClassExportDTO> classExportVOS = schoolGradeExportDTO.getChild();
            // 转换成班级Maps 把班级名称作为key
            Map<String, Integer> classExportMaps = classExportVOS.stream().collect(Collectors.toMap(SchoolClassExportDTO::getName, SchoolClassExportDTO::getId));
            Integer classId = classExportMaps.get(item.get(5));
            Assert.notNull(classId, "班级数据为空");
            // 设置班级信息
            schoolStudent.setClassId(classId);
            schoolStudent.setClassName(item.get(5));
            // 更新管理端
            Integer managementStudentId = updateManagementStudent(schoolStudent);
            schoolStudent.setStudentId(managementStudentId);
            schoolStudentService.saveOrUpdate(schoolStudent);
        }

    }

    /**
     * 更新管理端的学生信息
     *
     * @param schoolStudent 学校端学生
     * @return 管理端学生
     */
    public Integer updateManagementStudent(SchoolStudent schoolStudent) {
        // 通过身份证在管理端查找学生
        Student managementStudent = studentService.getAllByIdCard(schoolStudent.getIdCard());

        // 如果为空新增，否则是更新
        if (Objects.isNull(managementStudent)) {
            Student student = new Student();
            BeanUtils.copyProperties(schoolStudent, student);
            studentService.saveStudent(student);
            return student.getId();
        }
        managementStudent.setSchoolId(schoolStudent.getSchoolId());
        managementStudent.setSno(schoolStudent.getSno());
        managementStudent.setName(schoolStudent.getName());
        managementStudent.setGender(schoolStudent.getGender());
        managementStudent.setClassId(schoolStudent.getClassId());
        managementStudent.setGradeId(schoolStudent.getGradeId());
        managementStudent.setIdCard(schoolStudent.getIdCard());
        managementStudent.setBirthday(schoolStudent.getBirthday());
        managementStudent.setNation(schoolStudent.getNation());
        managementStudent.setParentPhone(schoolStudent.getParentPhone());
        managementStudent.setProvinceCode(schoolStudent.getProvinceCode());
        managementStudent.setCityCode(schoolStudent.getCityCode());
        managementStudent.setAreaCode(schoolStudent.getAreaCode());
        managementStudent.setTownCode(schoolStudent.getTownCode());
        managementStudent.setAddress(schoolStudent.getAddress());
        managementStudent.setStatus(CommonConst.STATUS_NOT_DELETED);
        studentService.updateStudent(managementStudent);
        return managementStudent.getId();
    }

    /**
     * 检查身份证、学号是否重复
     *
     * @param idCards 身份证
     * @param snoList 学号
     */
    private void checkIdCard(List<String> idCards, List<String> snoList) {
        if (CollectionUtils.isEmpty(idCards)) {
            throw new BusinessException("身份证为空");
        }
        if (CollectionUtils.isEmpty(snoList)) {
            throw new BusinessException("学号为空");
        }
        List<String> idCardDuplicate = ListUtil.getDuplicateElements(idCards);
        if (!CollectionUtils.isEmpty(idCardDuplicate)) {
            throw new BusinessException("身份证号码：" + String.join(",", idCardDuplicate) + "重复");
        }
        List<String> snoDuplicate = ListUtil.getDuplicateElements(snoList);
        if (!CollectionUtils.isEmpty(snoDuplicate)) {
            throw new BusinessException("学号：" + String.join(",", snoDuplicate) + "重复");
        }
    }

    /**
     * 学校端-学生是否存在
     *
     * @param snoMap    学号Map
     * @param idCardMap 身份证Map
     * @param sno       学号
     * @param idCard    身份证
     */
    private void checkIsExist(Map<String, SchoolStudent> snoMap, Map<String, SchoolStudent> idCardMap, String sno, String idCard, String gender, String birthday, String gradeName) {

        if (StringUtils.isAllBlank(sno, idCard)) {
            throw new BusinessException("学号或身份证为空");
        }
        if (Objects.nonNull(snoMap.get(sno))) {
            throw new BusinessException("学号" + sno + "在系统中重复");
        }
        if (Objects.nonNull(idCardMap.get(idCard))) {
            throw new BusinessException("身份证" + idCard + "在系统中重复");
        }
        if (StringUtils.isBlank(gender)) {
            throw new BusinessException("身份证" + idCard + "性别不能为空");
        }
        if (StringUtils.isBlank(birthday)) {
            throw new BusinessException("身份证" + idCard + "生日不能为空");
        }
        if (StringUtils.isBlank(gradeName)) {
            throw new BusinessException("身份证" + idCard + "年级不能为空");
        }
    }
    /**
     * 前置校验
     *
     * @param schools 学校列表
     * @param idCards 身份证信息
     */
    private void preCheckStudent(List<School> schools, List<String> idCards) {
        Assert.isTrue(!CollectionUtils.isEmpty(schools), "学校编号异常");

        List<String> notLegalIdCards = new ArrayList<>();
        idCards.forEach(s -> {
            if (!IdcardUtil.isValidCard(s)) {
                notLegalIdCards.add(s);
            }
        });
        if (!com.wupol.framework.core.util.CollectionUtils.isEmpty(notLegalIdCards)) {
            throw new BusinessException("身份证格式错误：" + notLegalIdCards);
        }

        List<String> duplicateElements = ListUtil.getDuplicateElements(idCards);
        if (!CollectionUtils.isEmpty(duplicateElements)) {
            throw new BusinessException("身份证" + StringUtils.join(duplicateElements, ",") + "重复");
        }

        List<String> repeatIdCard = idCards.stream().filter(s -> StringUtils.isNotBlank(s) && !Pattern.matches(RegularUtils.REGULAR_ID_CARD, s)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(repeatIdCard)) {
            throw new BusinessException("身份证" + StringUtils.join(repeatIdCard, ",") + "错误");
        }
    }

}