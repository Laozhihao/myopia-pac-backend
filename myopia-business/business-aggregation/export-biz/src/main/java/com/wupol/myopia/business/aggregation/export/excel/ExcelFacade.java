package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.alibaba.fastjson.JSONPath;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
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
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningResultPahtConst;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningDataContrastDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationStaffDTO;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
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

    private static final String FILE_SUFFIX = ".xlsx";

    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private NoticeService noticeService;
    @Resource
    private S3Utils s3Utils;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ExcelStudentService excelStudentService;

    /**
     * 导入学生
     *
     * @param createUserId  创建人userID
     * @param multipartFile 导入文件
     * @throws BusinessException 异常
     */
    public void importStudent(Integer createUserId, MultipartFile multipartFile) throws ParseException {
        String fileName = IOUtils.getTempPath() + multipartFile.getName() + "_" + System.currentTimeMillis() + FILE_SUFFIX;
        File file = new File(fileName);
        try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        } catch (IOException e) {
            log.error("导入学生数据异常:", e);
            throw new BusinessException("导入学生数据异常");
        }
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        List<Map<Integer, String>> listMap;
        try {
            listMap = EasyExcel.read(fileName).sheet().doReadSync();
        } catch (Exception e) {
            log.error("导入学生数据异常:", e);
            throw new BusinessException("Excel解析异常");
        }
        if (CollectionUtils.isEmpty(listMap)) {
            return;
        }
        if (!listMap.isEmpty()) {
            // 去头部
            listMap.remove(0);
        }
        // 收集学校编号
        List<String> schoolNos = listMap.stream().map(s -> s.get(4)).collect(Collectors.toList());
        List<School> schools = schoolService.getBySchoolNos(schoolNos);

        // 收集身份证号码
        List<String> idCards = listMap.stream().map(s -> s.get(8))
                .filter(Objects::nonNull).collect(Collectors.toList());

        preCheckStudent(schools, idCards);

        // 收集年级信息
        List<SchoolGradeExportDTO> grades = schoolGradeService.getBySchoolIds(schools.stream()
                .map(School::getId).collect(Collectors.toList()));
        schoolGradeService.packageGradeInfo(grades);

        // 通过学校编号分组
        Map<String, List<SchoolGradeExportDTO>> schoolGradeMaps = grades.stream()
                .collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolNo));

        List<Student> importList = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            Student student = new Student();
            if (StringUtils.isBlank(item.get(0))) {
                break;
            }
            checkStudentInfo(item);

            // excel 格式： 姓名	性别	出生日期	民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  ) 年级	班级	学号	身份证号	手机号码	省	市	县区	镇/街道	详细
            student.setName(item.get(0))
                    .setGender(GenderEnum.getType(item.get(1)))
                    .setBirthday(DateFormatUtil.parseDate(item.get(2), DateFormatUtil.FORMAT_ONLY_DATE2))
                    .setNation(NationEnum.getCode(item.get(3)))
                    .setSchoolNo(item.get(4))
                    .setGradeType(GradeCodeEnum.getByName(item.get(5)).getType())
                    .setSno((item.get(7)))
                    .setIdCard(item.get(8))
                    .setParentPhone(item.get(9))
                    .setCreateUserId(createUserId);
            student.setProvinceCode(districtService.getCodeByName(item.get(10)));
            student.setCityCode(districtService.getCodeByName(item.get(11)));
            student.setAreaCode(districtService.getCodeByName(item.get(12)));
            student.setTownCode(districtService.getCodeByName(item.get(13)));
            student.setAddress(item.get(14));

            // 通过学校编号获取改学校的年级信息
            List<SchoolGradeExportDTO> schoolGradeExportVOS = schoolGradeMaps.get(item.get(4));

            // 转换成年级Maps，年级名称作为Key
            Map<String, SchoolGradeExportDTO> gradeMaps = schoolGradeExportVOS.stream()
                    .collect(Collectors.toMap(SchoolGradeExportDTO::getName, Function.identity()));

            // 年级信息
            SchoolGradeExportDTO schoolGradeExportDTO = gradeMaps.get(item.get(5));
            if (null == schoolGradeExportDTO) {
                throw new BusinessException("年级数据异常");
            } else {
                // 设置年级ID
                student.setGradeId(schoolGradeExportDTO.getId());

                // 获取年级内的班级信息
                List<SchoolClassExportDTO> classExportVOS = schoolGradeExportDTO.getChild();

                // 转换成班级Maps 把班级名称作为key
                Map<String, Integer> classExportMaps = classExportVOS.stream()
                        .collect(Collectors.toMap(SchoolClassExportDTO::getName, SchoolClassExportDTO::getId));
                Integer classId = classExportMaps.get(item.get(6));
                if (Objects.isNull(classId)) {
                    throw new BusinessException("班级数据异常");
                } else {
                    // 设置班级信息
                    student.setClassId(classId);
                }
            }
            importList.add(student);
        }
        studentService.saveBatch(importList);
    }

    /**
     * 前置校验
     *
     * @param schools 学校列表
     * @param idCards 身份证信息
     */
    private void preCheckStudent(List<School> schools, List<String> idCards) {
        Assert.isTrue(CollectionUtils.isEmpty(schools), "学校编号异常");
        Assert.isTrue(idCards.stream().distinct().count() < idCards.size(), "学生身份证号码重复");
        Assert.isTrue(studentService.checkIdCards(idCards), "学生身份证号码重复");
    }


    /**
     * 导入机构人员
     *
     * @param currentUser    当前登录用户
     * @param multipartFile  导入文件
     * @param screeningOrgId 筛查机构id
     * @throws BusinessException io异常
     */
    public void importScreeningOrganizationStaff(CurrentUser currentUser, MultipartFile multipartFile,
                                                 Integer screeningOrgId) {
        if (null == screeningOrgId) {
            throw new BusinessException("机构ID不能为空");
        }

        String fileName = IOUtils.getTempPath() + multipartFile.getName() + "_" + System.currentTimeMillis() + FILE_SUFFIX;
        File file = new File(fileName);
        try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        } catch (IOException e) {
            log.error("导入人员数据异常:", e);
            throw new BusinessException("导入人员数据异常");
        }
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        List<Map<Integer, String>> listMap;
        try {
            listMap = EasyExcel.read(fileName).sheet().doReadSync();
        } catch (ExcelAnalysisException excelAnalysisException) {
            log.error("导入机构人员异常", excelAnalysisException);
            throw new BusinessException("解析文件格式异常");
        } catch (Exception e) {
            log.error("导入机构人员异常", e);
            throw new BusinessException("解析Excel文件异常");
        }
        if (!listMap.isEmpty()) { // 去头部
            listMap.remove(0);
        }

        preCheckStaff(screeningOrgId, listMap);

        // excel格式：序号	姓名	性别	身份证号	手机号码	说明
        List<UserDTO> userList = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            if (StringUtils.isBlank(item.get(0))) {
                break;
            }
            checkStaffInfo(item);
            UserDTO userDTO = new UserDTO();
            userDTO.setRealName(item.get(0))
                    .setGender(GenderEnum.getType(item.get(1)))
                    .setIdCard(item.get(2))
                    .setPhone(item.get(3))
                    .setCreateUserId(currentUser.getId())
                    .setIsLeader(0)
                    .setPassword(PasswordGenerator.getScreeningUserPwd(item.get(3), item.get(2)))
                    .setUsername(item.get(3))
                    .setOrgId(screeningOrgId)
                    .setSystemCode(SystemCode.SCREENING_CLIENT.getCode());
            if (null != item.get(4)) {
                userDTO.setRemark(item.get(4));
            }
            userList.add(userDTO);
        }
        List<ScreeningOrganizationStaffDTO> importList = userList.stream().map(item -> {
            ScreeningOrganizationStaffDTO staff = new ScreeningOrganizationStaffDTO();
            staff.setIdCard(item.getIdCard())
                    .setScreeningOrgId(item.getOrgId())
                    .setCreateUserId(item.getCreateUserId())
                    .setRemark(item.getRemark())
                    .setGovDeptId(currentUser.getOrgId());
            return staff;
        }).collect(Collectors.toList());

        // 批量新增OAuth2
        List<User> users = oauthServiceClient.addScreeningUserBatch(userList);
        Map<String, Integer> userMaps = users.stream()
                .collect(Collectors.toMap(User::getIdCard, User::getId));
        // 设置userId
        importList.forEach(i -> i.setUserId(userMaps.get(i.getIdCard())));
        screeningOrganizationStaffService.saveBatch(importList);
    }

    /**
     * 筛查人员前置校验
     *
     * @param screeningOrgId 筛查机构ID
     * @param listMap        筛查人员
     */
    private void preCheckStaff(Integer screeningOrgId, List<Map<Integer, String>> listMap) {
        // 收集身份证号码
        List<String> idCards = listMap.stream().map(s -> s.get(2)).collect(Collectors.toList());
        if (idCards.stream().distinct().count() < idCards.size()) {
            throw new BusinessException("身份证号码重复");
        }
        List<User> checkIdCards = oauthServiceClient.getUserBatchByIdCards(idCards,
                SystemCode.SCREENING_CLIENT.getCode(), screeningOrgId);
        Assert.isTrue(!CollectionUtils.isEmpty(checkIdCards), "身份证号码已经被使用，请确认！");

        // 收集手机号码
        List<String> phones = listMap.stream().map(s -> s.get(3)).collect(Collectors.toList());
        Assert.isTrue(phones.stream().distinct().count() < phones.size(), "手机号码重复");

        List<User> checkPhones = oauthServiceClient.getUserBatchByPhones(phones, SystemCode.SCREENING_CLIENT.getCode());
        Assert.isTrue(!CollectionUtils.isEmpty(checkPhones), "手机号码已经被使用，请确认！");

    }

    /**
     * 检查筛查人员信息
     *
     * @param item 筛查人员
     */
    private void checkStaffInfo(Map<Integer, String> item) {
        Assert.isTrue(StringUtils.isBlank(item.get(1)) || GenderEnum.getType(item.get(1)).equals(0), "性别异常");
        Assert.isTrue(StringUtils.isBlank(item.get(2)) || !Pattern.matches(RegularUtils.REGULAR_ID_CARD, item.get(2)), "身份证异常");
        Assert.isTrue(StringUtils.isBlank(item.get(3)) || !Pattern.matches(RegularUtils.REGULAR_MOBILE, item.get(3)), "手机号码异常");
    }

    /**
     * 导入筛查学校的学生信息
     *
     * @param userId
     * @param multipartFile
     * @param schoolId
     * @throws IOException
     */
    public void importScreeningSchoolStudents(Integer userId, MultipartFile multipartFile, ScreeningPlan screeningPlan, Integer schoolId) throws IOException {
        String fileName = IOUtils.getTempPath() + multipartFile.getName() + "_" + System.currentTimeMillis() + FILE_SUFFIX;
        File file = new File(fileName);
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        List<Map<Integer, String>> listMap;
        try {
            listMap = EasyExcel.read(fileName).sheet().doReadSync();
        } catch (ExcelAnalysisException excelAnalysisException) {
            log.error("导入筛查学生数据异常", excelAnalysisException);
            throw new BusinessException("解析文件格式异常");
        } catch (Exception e) {
            log.error("导入筛查学生数据异常", e);
            throw new BusinessException("解析Excel文件异常");
        }
        if (!listMap.isEmpty()) {
            // 去头部
            listMap.remove(0);
        }
        if (CollectionUtils.isEmpty(listMap)) {
            // 无数据，直接返回
            return;
        }
        // 这里是Excel的一个小坑
        List<Map<Integer, String>> resultList = listMap.stream().filter(s -> s.get(ImportExcelEnum.NAME.getIndex()) != null).collect(Collectors.toList());
        excelStudentService.insertByUpload(userId, resultList, screeningPlan, schoolId);
        screeningPlanService.updateStudentNumbers(userId, screeningPlan.getId(), screeningPlanSchoolStudentService.getCountByScreeningPlanId(screeningPlan.getId()));
    }

    /**
     * 导出统计报表 - 数据对比表
     *
     * @param userId     用户ID
     * @param exportList 导出数据
     * @param template   导出模板
     * @throws IOException
     * @throws UtilException
     */
    public void exportStatContrast(Integer userId, List<ScreeningDataContrastDTO> exportList,
                                   InputStream template) throws IOException, UtilException {
        String fileName = "统计对比报表";
        log.info("导出统计对比报文件: {}", fileName);
        File file = ExcelUtil.exportHorizonListToExcel(fileName, exportList, template);
        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS, fileName, new Date());
        noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * @param userId
     * @param statConclusionExportDTOs
     * @param isSchoolExport           是否学校维度导出
     * @param districtOrSchoolName
     * @throws IOException
     * @throws UtilException
     */
    @Async
    public void generateVisionScreeningResult(Integer userId, List<StatConclusionExportDTO> statConclusionExportDTOs, boolean isSchoolExport, String districtOrSchoolName) throws IOException, UtilException {
        // 设置导出的文件名
        String fileName = String.format("%s-筛查数据", districtOrSchoolName);
        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS, districtOrSchoolName + "筛查数据", new Date());
        log.info("导出筛查结果文件: {}", fileName);
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);
        if (isSchoolExport) {
            List<VisionScreeningResultExportDTO> visionScreeningResultExportVos = genVisionScreeningResultExportVos(statConclusionExportDTOs);
            File excelFile = ExcelUtil.exportListToExcel(fileName, visionScreeningResultExportVos, mergeStrategy, VisionScreeningResultExportDTO.class);
            noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(excelFile), CommonConst.NOTICE_STATION_LETTER);
        } else {
            String folder = String.format("%s-%s", System.currentTimeMillis(), UUID.randomUUID());
            Map<String, List<StatConclusionExportDTO>> schoolNameMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getSchoolName));
            schoolNameMap.keySet().forEach(schoolName -> {
                List<VisionScreeningResultExportDTO> visionScreeningResultExportVos = genVisionScreeningResultExportVos(schoolNameMap.getOrDefault(schoolName, Collections.emptyList()));
                String excelFileName = String.format("%s-筛查数据", schoolName);
                try {
                    ExcelUtil.exportListToExcelWithFolder(folder, excelFileName, visionScreeningResultExportVos, mergeStrategy, VisionScreeningResultExportDTO.class);
                } catch (Exception e) {
                    log.error(e);
                }
            });
            File zipFile = ExcelUtil.zip(folder, fileName);
            noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(zipFile), CommonConst.NOTICE_STATION_LETTER);
        }
    }

    /**
     * 生成筛查数据
     *
     * @param statConclusionExportDTOs
     * @return
     */
    private List<VisionScreeningResultExportDTO> genVisionScreeningResultExportVos(List<StatConclusionExportDTO> statConclusionExportDTOs) {
        Map<Boolean, List<StatConclusionExportDTO>> isRescreenMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getIsRescreen));
        Map<Integer, StatConclusionExportDTO> rescreenPlanStudentIdVoMap = isRescreenMap.getOrDefault(true, Collections.emptyList()).stream().collect(Collectors.toMap(StatConclusionExportDTO::getScreeningPlanSchoolStudentId, Function.identity(), (x, y) -> x));
        List<VisionScreeningResultExportDTO> exportVos = new ArrayList<>();
        List<StatConclusionExportDTO> vos = isRescreenMap.getOrDefault(false, Collections.emptyList());
        for (int i = 0; i < vos.size(); i++) {
            StatConclusionExportDTO vo = vos.get(i);
            VisionScreeningResultExportDTO exportVo = new VisionScreeningResultExportDTO();
            BeanUtils.copyProperties(vo, exportVo);
            GlassesType glassesType = GlassesType.get(vo.getGlassesType());
            exportVo.setId(i + 1)
                    .setGenderDesc(GenderEnum.getName(vo.getGender()))
                    .setNationDesc(StringUtils.defaultString(NationEnum.getName(vo.getNation())))
                    .setGlassesTypeDesc(Objects.isNull(glassesType) ? "--" : glassesType.desc).setIsRescreenDesc("否")
                    .setWarningLevelDesc(StringUtils.defaultIfBlank(WarningLevel.getDesc(vo.getWarningLevel()), "--"))
                    .setParentPhone(vo.getParentPhone())
                    .setAddress(vo.getAddress());
            genScreeningData(vo, exportVo);
            genReScreeningData(rescreenPlanStudentIdVoMap, vo, exportVo);
            genDate(vo, exportVo);
            exportVos.add(exportVo);
        }
        return exportVos;
    }

    private void genDate(StatConclusionExportDTO dto, VisionScreeningResultExportDTO exportDTO) {
        exportDTO.setEsotropia(generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OID_ESOTROPIA)));
        exportDTO.setExotropia(generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OID_EXOTROPIA)));
        exportDTO.setVerticalStrabismus(generateSingleEyeDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OID_VERTICAL_STRABISMUS)));
        exportDTO.setOdiagnosis(singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OID_DIAGNOSIS)));

        exportDTO.setVdiagnosis(singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_VD_DIAGNOSIS)));

        exportDTO.setCdiagnosis(singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_CO_diagnosis)));

        exportDTO.setSleftEye(objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SLD_LEFT_PATHOLOGICAL_TISSUES)));
        exportDTO.setSleftResult(singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SLD_LEFT_DIAGNOSIS)));
        exportDTO.setSrightEye(objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SLD_RIGHT_PATHOLOGICAL_TISSUES)));
        exportDTO.setSrightResult(singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SLD_RIGHT_DIAGNOSIS)));

        exportDTO.setPsph(generateSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_SPN), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_SPN)));
        exportDTO.setPcyl(generateSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_CYL), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_CYL)));
        exportDTO.setPaxial(generateEyesDegree(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_AXIAL), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_AXIAL)));
        exportDTO.setPcorrectedVision(biometricsDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_CORRECTEDVISION), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_CORRECTEDVISION)));
        exportDTO.setPdiagnosis(singleDiagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_DIAGNOSIS)));
        exportDTO.setPresult(StatUtil.getRefractiveResult((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_SPN), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_LEFT_CYL),
                (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_SPN), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_POD_RIGHT_CYL), DateUtil.ageOfNow(dto.getBirthday())));
        exportDTO.setDbK1(genCornealCurvature(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K1), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K1_AXIS),
                JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K1), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K1_AXIS)));
        exportDTO.setDbK2(genCornealCurvature(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K2), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K2_AXIS),
                JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K2), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K2_AXIS)));
        exportDTO.setDbAST(genCornealCurvature(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_AST), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_K1_AXIS),
                JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_AST), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_K1_AXIS)));
        exportDTO.setDbPD(generateSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_PD), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_PD)));
        exportDTO.setDbWTW(generateSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_WTW), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_WTW)));
        exportDTO.setDbAL(generateSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_AL), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_AL)));
        exportDTO.setDbCCT(generateSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_CCT), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_CCT)));
        exportDTO.setDbAD(generateSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_AD), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_AD)));
        exportDTO.setDbLT(generateSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_LT), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_LT)));
        exportDTO.setDbVT(generateSuffixMMStr(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_LEFT_VT), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_BD_RIGHT_VT)));

        exportDTO.setIpDate(biometricsDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_IPD_RIGHT_PRESSURE), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_IPD_LEFT_PRESSURE)));
        exportDTO.setFdDate(diagnosis2String((Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_DF_RIGHT_HASABNORMAL), (Integer) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_DF_LEFT_HASABNORMAL)));

        exportDTO.setLeftEyeDiseases(objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OED_LEFT_EYE_DISEASES)));
        exportDTO.setRightEyeDiseases(objectList2Str(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_OED_RIGHT_EYE_DISEASES)));
        exportDTO.setSystemicDiseaseSymptom((String) JSONPath.eval(dto, ScreeningResultPahtConst.PATH_SYSTEMIC_DISEASE_SYMPTOM));
        exportDTO.setLevel(levelDateFormat(JSONPath.eval(dto, ScreeningResultPahtConst.PATH_VLLD_RIGHT_LEVEL), JSONPath.eval(dto, ScreeningResultPahtConst.PATH_VLLD_LEFT_LEVEL)));
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
     * 生成双眼度数String，后缀为°
     *
     * @param val1 值
     * @param val2 值
     * @return String
     */
    private String generateEyesDegree(Object val1, Object val2) {
        return generateSingleEyeDegree(val1) + "/" + generateSingleEyeDegree(val2);
    }

    /**
     * 双眼后缀为D
     *
     * @param val1 值
     * @param val2 值
     * @return String
     */
    private String generateSuffixDStr(Object val1, Object val2) {
        return generateSingleSuffixDStr(val1) + "/" + generateSingleSuffixDStr(val2);
    }

    /**
     * 单眼后缀为D
     *
     * @param val 值
     * @return String
     */
    private String generateSingleSuffixDStr(Object val) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return (Objects.nonNull(val) ? decimalFormat.format(val) + "D" : "--");
    }

    /**
     * 双眼后缀为mm
     *
     * @param val1 值
     * @param val2 值
     * @return String
     */
    private String generateSuffixMMStr(Object val1, Object val2) {
        return generateSingleSuffixMMStr(val1) + "/" + generateSingleSuffixMMStr(val2);
    }

    /**
     * 单眼后缀为mm
     *
     * @param val 值
     * @return String
     */
    private String generateSingleSuffixMMStr(Object val) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return (Objects.nonNull(val) ? decimalFormat.format(new BigDecimal((String) val)) + "mm" : "--");
    }

    /**
     * 组装复筛数据
     *
     * @param rescreenPlanStudentIdDTOMap
     * @param dto
     * @param exportDTO
     */
    private void genReScreeningData(Map<Integer, StatConclusionExportDTO> rescreenPlanStudentIdDTOMap, StatConclusionExportDTO dto, VisionScreeningResultExportDTO exportDTO) {
        StatConclusionExportDTO rescreenVo = rescreenPlanStudentIdDTOMap.get(dto.getScreeningPlanSchoolStudentId());
        if (Objects.nonNull(rescreenVo)) {
            exportDTO.setReScreenNakedVisions(eyeDataFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION), (BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION), 1))
                    .setReScreenCorrectedVisions(eyeDataFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_CORRECTED_VISION), (BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_CORRECTED_VISION), 1))
                    .setReScreenSphs(eyeDataFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_SPH), (BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_SPH), 2))
                    .setReScreenCyls(eyeDataFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_CYL), (BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_CYL), 2))
                    .setReScreenAxials(eyeDataFormat((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_AXIAL), (BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_AXIAL), 0))
                    .setReScreenSphericalEquivalents(eyeDataFormat(StatUtil.getSphericalEquivalent((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_SPH), (BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.RIGHTEYE_CYL)), StatUtil.getSphericalEquivalent((BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_SPH), (BigDecimal) JSONPath.eval(rescreenVo, ScreeningResultPahtConst.LEFTEYE_CYL)), 2))
                    .setIsRescreenDesc("是");
        }
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
     * 初步结果 双眼
     *
     * @param val1 值1
     * @param val2 值2
     * @return String
     */
    private String diagnosis2String(Integer val1, Integer val2) {
        return singleDiagnosis2String(val1) + "/" + singleDiagnosis2String(val2);
    }


    /**
     * 组装初筛数据
     *
     * @param dto
     * @param exportDTO
     */
    private void genScreeningData(StatConclusionExportDTO dto, VisionScreeningResultExportDTO exportDTO) {
        exportDTO.setNakedVisions(eyeDataFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION), 1))
                .setCorrectedVisions(eyeDataFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CORRECTED_VISION), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CORRECTED_VISION), 1))
                .setSphs(generateSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_SPH), JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_SPH)))
                .setCyls(generateSuffixDStr(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CYL), JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CYL)))
                .setAxials(generateEyesDegree(JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_AXIAL), JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_AXIAL)));
        exportDTO.setCresult(StatUtil.getRefractiveResult((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_SPH), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CYL),
                (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_SPH), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CYL), DateUtil.ageOfNow(dto.getBirthday())));
//                .setSphericalEquivalents(eyeDataFormat(StatUtil.getSphericalEquivalent((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_SPH), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CYL)), StatUtil.getSphericalEquivalent((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_SPH), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CYL)), 2));
    }

    /**
     * 眼别数据格式化
     *
     * @param rightEyeData
     * @param leftEyeData
     * @return
     */
    private String eyeDataFormat(BigDecimal rightEyeData, BigDecimal leftEyeData, int scale) {
        // 不足两位小数补0
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if (scale == 0) {
            decimalFormat = new DecimalFormat("#");
        }
        if (scale == 1) {
            decimalFormat = new DecimalFormat("0.0");
        }
        return String.format("%s/%s", Objects.isNull(rightEyeData) ? "--" : decimalFormat.format(rightEyeData), Objects.isNull(leftEyeData) ? "--" : decimalFormat.format(leftEyeData));
    }

    /**
     * 格式化生物测量数据
     *
     * @param rightDate 右眼数据
     * @param leftDate  左眼数据
     * @return String
     */
    private String biometricsDateFormat(Object rightDate, Object leftDate) {
        return String.format("%s/%s", Objects.isNull(rightDate) ? "--" : rightDate, Objects.isNull(leftDate) ? "--" : leftDate);
    }

    /**
     * 格式化等级数据
     *
     * @param rightDate 右眼数据
     * @param leftDate  左眼数据
     * @return String
     */
    private String levelDateFormat(Object rightDate, Object leftDate) {
        return String.format("%s/%s", Objects.isNull(rightDate) ? "--" : rightDate + "级", Objects.isNull(leftDate) ? "--" : leftDate + "级");
    }

    /**
     * 检查学生信息是否完整
     *
     * @param item 学生信息
     */
    private void checkStudentInfo(Map<Integer, String> item) {
        Assert.isTrue(StringUtils.isBlank(item.get(1)) || GenderEnum.getType(item.get(1)).equals(-1), "学生性别异常");
        Assert.isTrue(StringUtils.isBlank(item.get(2)), "学生出生日期不能为空");
        Assert.isTrue(StringUtils.isBlank(item.get(4)), "学校编号不能为空");
        Assert.isTrue(StringUtils.isBlank(item.get(5)), "学生年级不能为空");
        Assert.isTrue(StringUtils.isBlank(item.get(6)), "学生班级不能为空");
        Assert.isTrue(StringUtils.isBlank(item.get(7)), "学生学号异常");
        Assert.isTrue(StringUtils.isBlank(item.get(8)) || !Pattern.matches(RegularUtils.REGULAR_ID_CARD, item.get(8)), "学生身份证异常");
        Assert.isTrue(StringUtils.isNotBlank(item.get(9)) && !Pattern.matches(RegularUtils.REGULAR_MOBILE, item.get(9)), "学生手机号码异常");
    }

    /**
     * 角膜曲率（双眼）
     *
     * @param k1Left      角膜前表面曲率K1
     * @param k1AxisLeft  角膜前表面曲率K1的度数
     * @param k1Right     角膜前表面曲率K2
     * @param k1AxisRight 角膜前表面曲率K2的度数
     * @return String
     */
    private String genCornealCurvature(Object k1Left, Object k1AxisLeft, Object k1Right, Object k1AxisRight) {
        return String.format("%s/%s", genEyeCornealCurvature(k1Right, k1AxisRight), genEyeCornealCurvature(k1Left, k1AxisLeft));
    }


    /**
     * 角膜曲率（单眼）
     *
     * @param val1 值1
     * @param val2 值2
     * @return String
     */
    private String genEyeCornealCurvature(Object val1, Object val2) {
        return String.format("%sD@%S°", Objects.nonNull(val1) ? val1 : "--", Objects.nonNull(val2) ? val2 : "--");
    }
}
