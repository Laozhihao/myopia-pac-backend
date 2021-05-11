package com.wupol.myopia.business.aggregation.export.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.*;
import com.wupol.myopia.business.aggregation.export.constant.ImportExcelEnum;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.core.common.constant.ExportAddressKey;
import com.wupol.myopia.business.core.common.domain.model.AddressCode;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.hospital.constant.HospitalEnum;
import com.wupol.myopia.business.core.hospital.constant.HospitalLevelEnum;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalExportDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.dto.*;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningResultPahtConst;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningDataContrastDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningCountDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.screening.organization.ScreeningOrgConfigTypeEnum;
import com.wupol.myopia.business.core.screening.organization.ScreeningOrganizationEnum;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationExportDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationStaffDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationStaffExportDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
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
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ExcelStudentService excelStudentService;
    @Resource
    private MedicalReportService medicalReportService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    /**
     * 生成筛查机构Excel
     *
     * @param userId     创建人
     * @param districtId 地区id
     **/
    @Async
    public void generateScreeningOrganization(Integer userId, Integer districtId) throws IOException, UtilException {
        District district = checkAndGetDistrict(districtId);
        // 设置文件名
        String fileName = "筛查机构" + "-" + district.getName();
        // 查询数据
        ScreeningOrganizationQueryDTO query = new ScreeningOrganizationQueryDTO();
        query.setDistrictId(districtId);
        List<ScreeningOrganization> list = screeningOrganizationService.getBy(query);
        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS, districtService.getTopDistrictName(district.getCode()) + "筛查机构数据表", new Date());
        if (CollectionUtils.isEmpty(list)) {
            File file = ExcelUtil.exportListToExcel(fileName, new ArrayList<>(), ScreeningOrganizationExportDTO.class);
            noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
            return;
        }

        // 获取筛查人员信息
        Map<Integer, List<ScreeningOrganizationStaff>> staffMaps = screeningOrganizationStaffService
                .getOrgStaffMapByIds(list.stream().map(ScreeningOrganization::getId)
                        .collect(Collectors.toList()));

        // 创建人姓名
        Set<Integer> createUserIds = list.stream()
                .map(ScreeningOrganization::getCreateUserId)
                .collect(Collectors.toSet());
        Map<Integer, User> userMap = getUserMapByIds(createUserIds);

        List<ScreeningOrganizationExportDTO> exportList = new ArrayList<>();
        for (ScreeningOrganization item : list) {
            HashMap<String, String> addressMap = generateAddressMap(item);
            ScreeningOrganizationExportDTO exportVo = new ScreeningOrganizationExportDTO();
            exportVo.setName(item.getName())
                    .setType(ScreeningOrganizationEnum.getTypeName(item.getType()))
                    .setConfigType(ScreeningOrgConfigTypeEnum.getTypeName(item.getConfigType()))
                    .setPhone(item.getPhone())
                    .setRemark(item.getRemark())
                    .setDistrictName(districtService.getDistrictName(item.getDistrictDetail()))
                    .setAddress(item.getAddress())
                    .setCreateUser(userMap.get(item.getCreateUserId()).getRealName())
                    .setCreateTime(DateFormatUtil.format(item.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME))
                    .setProvince(addressMap.getOrDefault(ExportAddressKey.PROVIDE, StringUtils.EMPTY))
                    .setCity(addressMap.getOrDefault(ExportAddressKey.CITY, StringUtils.EMPTY))
                    .setArea(addressMap.getOrDefault(ExportAddressKey.AREA, StringUtils.EMPTY))
                    .setTown(addressMap.getOrDefault(ExportAddressKey.TOWN, StringUtils.EMPTY));
            List<ScreeningPlan> planResult = screeningPlanService.getByOrgId(item.getId());
            exportVo.setScreeningCount(CollectionUtils.isEmpty(planResult) ? 0 : planResult.size());
            if (Objects.nonNull(staffMaps.get(item.getId()))) {
                exportVo.setPersonSituation(staffMaps.get(item.getId()).size());
            } else {
                exportVo.setPersonSituation(0);
            }
            exportList.add(exportVo);
        }
        log.info("导出筛查机构文件: {}", fileName);
        File file = ExcelUtil.exportListToExcel(fileName, exportList, ScreeningOrganizationExportDTO.class);
        noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * 生成筛查机构人员Excel
     *
     * @param userId         创建人
     * @param screeningOrgId 机构id
     **/
    public void generateScreeningOrganizationStaff(Integer userId, Integer screeningOrgId) throws IOException, UtilException {
        if (Objects.isNull(screeningOrgId)) {
            throw new BusinessException("筛查机构id不能为空");
        }
        List<ScreeningOrganizationStaff> staffLists = screeningOrganizationStaffService.getByOrgId(screeningOrgId);
        UserDTO userQuery = new UserDTO();
        userQuery.setSize(staffLists.size())
                .setCurrent(1)
                .setOrgId(screeningOrgId)
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode());
        Page<User> userPage = oauthServiceClient.getUserListPage(userQuery);
        List<User> userList = JSONObject.parseArray(JSONObject.toJSONString(userPage.getRecords()), User.class);
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("筛查机构人员");
        String orgName = screeningOrganizationService.getById(screeningOrgId).getName();
        builder.append("-").append(orgName);
        String fileName = builder.toString();

        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS, orgName + "筛查机构人员数据表", new Date());
        if (CollectionUtils.isEmpty(userList)) {
            File file = ExcelUtil.exportListToExcel(fileName, new ArrayList<>(), ScreeningOrganizationStaffExportDTO.class);
            noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
            return;
        }

        // 获取完整的用户信息
        // 构建数据
        List<ScreeningOrganizationStaffExportDTO> exportList = userList.stream()
                .map(item -> new ScreeningOrganizationStaffExportDTO()
                        .setName(item.getRealName())
                        .setGender(GenderEnum.getName(item.getGender()))
                        .setPhone(item.getPhone())
                        .setIdCard(item.getIdCard())
                        .setOrganization(orgName)).collect(Collectors.toList());
        log.info("导出筛查机构人员文件: {}", fileName);
        File file = ExcelUtil.exportListToExcel(fileName, exportList, ScreeningOrganizationStaffExportDTO.class);
        noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * 生成医院Excel
     *
     * @param userId     创建人
     * @param districtId 地区id
     **/
    public void generateHospital(Integer userId, Integer districtId) throws IOException, UtilException {
        District district = checkAndGetDistrict(districtId);
        // 设置文件名
        String fileName = "医院" + "-" + district.getName();
        List<HospitalExportDTO> exportList = new ArrayList<>();
        HospitalQuery query = new HospitalQuery();
        query.setDistrictId(districtId);
        List<Hospital> list = hospitalService.getBy(query);

        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS, districtService.getTopDistrictName(district.getCode()) + "医院数据", new Date());
        if (CollectionUtils.isEmpty(list)) {
            File file = ExcelUtil.exportListToExcel(fileName, new ArrayList<>(), HospitalExportDTO.class);
            noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
            return;
        }

        // 创建人姓名
        Set<Integer> createUserIds = list.stream().map(Hospital::getCreateUserId).collect(Collectors.toSet());
        Map<Integer, User> userMap = getUserMapByIds(createUserIds);

        for (Hospital item : list) {
            HashMap<String, String> addressMap = generateAddressMap(item);
            HospitalExportDTO exportVo = new HospitalExportDTO()
                    .setName(item.getName())
                    .setDistrictName(districtService.getDistrictName(item.getDistrictDetail()))
                    .setLevel(HospitalLevelEnum.getLevel(item.getLevel()))
                    .setType(HospitalEnum.getTypeName(item.getType()))
                    .setKind(HospitalEnum.getKindName(item.getKind()))
                    .setRemark(item.getRemark())
                    .setAccountNo(item.getName())
                    .setAddress(item.getAddress())
                    .setCreateUser(userMap.get(item.getCreateUserId()).getRealName())
                    .setCreateTime(DateFormatUtil.format(item.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME))
                    .setProvince(addressMap.getOrDefault(ExportAddressKey.PROVIDE, StringUtils.EMPTY))
                    .setCity(addressMap.getOrDefault(ExportAddressKey.CITY, StringUtils.EMPTY))
                    .setArea(addressMap.getOrDefault(ExportAddressKey.AREA, StringUtils.EMPTY))
                    .setTown(addressMap.getOrDefault(ExportAddressKey.TOWN, StringUtils.EMPTY));
            exportList.add(exportVo);
        }
        File file = ExcelUtil.exportListToExcel(fileName, exportList, HospitalExportDTO.class);
        noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * 生成学校Excel
     *
     * @param userId     创建人
     * @param districtId 地区id
     **/
    public void generateSchool(Integer userId, Integer districtId) throws IOException, UtilException {
        District district = checkAndGetDistrict(districtId);
        // 设置文件名
        String fileName = "学校" + "-" + district.getName();
        SchoolQueryDTO query = new SchoolQueryDTO();
        query.setDistrictId(districtId);
        List<School> list = schoolService.getBy(query);

        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS, districtService.getTopDistrictName(district.getCode()) + "学校数据", new Date());
        if (CollectionUtils.isEmpty(list)) {
            File file = ExcelUtil.exportListToExcel(fileName, new ArrayList<>(), SchoolExportDTO.class);
            noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
            return;
        }

        List<Integer> schoolIds = list.stream().map(School::getId).collect(Collectors.toList());
        Set<Integer> createUserIds = list.stream().map(School::getCreateUserId).collect(Collectors.toSet());

        // 创建人姓名
        Map<Integer, User> userMap = getUserMapByIds(createUserIds);

        // 学生统计
        List<StudentCountDTO> studentCountVOS = studentService.countStudentBySchoolNo();
        Map<String, Integer> studentCountMaps = studentCountVOS.stream()
                .collect(Collectors.toMap(StudentCountDTO::getSchoolNo, StudentCountDTO::getCount));

        // 年级统计
        List<SchoolGradeExportDTO> grades = schoolGradeService.getBySchoolIds(schoolIds);
        packageGradeInfo(grades);

        // 年级通过学校ID分组
        Map<Integer, List<SchoolGradeExportDTO>> gradeMaps = grades.stream()
                .collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolId));

        // 学校筛查次数
        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getBySchoolIds(schoolIds);
        Map<Integer, Long> planSchoolMaps = planSchoolList.stream()
                .collect(Collectors.groupingBy(ScreeningPlanSchool::getSchoolId, Collectors.counting()));

        List<SchoolExportDTO> exportList = new ArrayList<>();
        for (School item : list) {
            HashMap<String, String> addressMap = generateAddressMap(item);
            SchoolExportDTO exportVo = new SchoolExportDTO()
                    .setNo(item.getSchoolNo())
                    .setName(item.getName())
                    .setKind(SchoolEnum.getKindName(item.getKind()))
                    .setType(SchoolEnum.getTypeName(item.getType()))
                    .setStudentCount(studentCountMaps.getOrDefault(item.getSchoolNo(), 0))
                    .setDistrictName(districtService.getDistrictName(item.getDistrictDetail()))
                    .setAddress(item.getAddress())
                    .setRemark(item.getRemark())
                    .setScreeningCount(planSchoolMaps.getOrDefault(item.getId(), 0L))
                    .setCreateUser(userMap.get(item.getCreateUserId()).getRealName())
                    .setCreateTime(DateFormatUtil.format(item.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME))
                    .setProvince(addressMap.getOrDefault(ExportAddressKey.PROVIDE, StringUtils.EMPTY))
                    .setCity(addressMap.getOrDefault(ExportAddressKey.CITY, StringUtils.EMPTY))
                    .setArea(addressMap.getOrDefault(ExportAddressKey.AREA, StringUtils.EMPTY))
                    .setTown(addressMap.getOrDefault(ExportAddressKey.TOWN, StringUtils.EMPTY));
            StringBuilder result = new StringBuilder();
            List<SchoolGradeExportDTO> exportGrade = gradeMaps.get(item.getId());
            if (!CollectionUtils.isEmpty(exportGrade)) {
                getSchoolGradeAndClass(result, exportGrade);
                exportVo.setClassName(result.toString());
            }
            if (Objects.nonNull(item.getLodgeStatus())) {
                exportVo.setLodgeStatus(SchoolEnum.getLodgeName(item.getLodgeStatus()));
            }
            exportList.add(exportVo);
        }
        log.info("导出学校文件: {}", fileName);
        File file = ExcelUtil.exportListToExcel(fileName, exportList, SchoolExportDTO.class);
        noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * 获取当前学校下的年级和班级
     *
     * @param result      结果
     * @param exportGrade 年级和班级信息
     */
    private void getSchoolGradeAndClass(StringBuilder result, List<SchoolGradeExportDTO> exportGrade) {
        exportGrade.forEach(grade -> {
            result.append(grade.getName()).append(": ");
            List<SchoolClassExportDTO> child = grade.getChild();
            for (int i = 0; i < child.size(); i++) {
                result.append(child.get(i).getName());
                if (i < child.size() - 1) {
                    result.append("、");
                } else {
                    result.append("。");
                }
            }
        });
    }

    /**
     * 生成学生Excel
     *
     * @param userId   创建人
     * @param schoolId 学校id
     * @param gradeId  年级id
     **/
    public void generateStudent(Integer userId, Integer schoolId, Integer gradeId) throws IOException, UtilException {
        if (Objects.isNull(schoolId)) {
            throw new BusinessException("学校id不能为空");
        }
        if (Objects.isNull(gradeId)) {
            throw new BusinessException("年级id不能为空");
        }
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("学生");
        School school = schoolService.getById(schoolId);
        String schoolName = school.getName();
        String gradeName = schoolGradeService.getById(gradeId).getName();
        builder.append("-").append(schoolName);
        builder.append("-").append(gradeName);
        String fileName = builder.toString();

        // 行政区域
        District district = districtService.findOne(new District().setId(school.getDistrictId()));

        // 查询学生
        List<StudentDTO> studentLists = studentService.getBySchoolIdAndGradeIdAndClassId(schoolId, null, gradeId);

        // 为空直接导出
        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS,
                districtService.getTopDistrictName(district.getCode()) + schoolName + gradeName + "学生数据表", new Date());
        if (CollectionUtils.isEmpty(studentLists)) {
            File file = ExcelUtil.exportListToExcel(fileName, new ArrayList<>(), StudentExportDTO.class);
            noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
            return;
        }
        // 获取年级班级信息
        List<Integer> classIdList = studentLists.stream().map(StudentDTO::getClassId).collect(Collectors.toList());
        Map<Integer, SchoolClass> classMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(classIdList)) {
            classMap = schoolClassService.getClassMapByIds(classIdList);
        }

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
                    .setGrade(gradeName)
                    .setIdCard(item.getIdCard())
                    .setBindPhone(item.getMpParentPhone())
                    .setPhone(item.getParentPhone())
                    .setAddress(item.getAddress())
                    .setLabel(WarningLevel.getDesc(item.getVisionLabel()))
                    .setSituation(item.situation2Str())
                    .setScreeningCount(countMaps.getOrDefault(item.getId(), 0))
                    .setQuestionCount(0)
                    .setLastScreeningTime(null)
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
        File file = ExcelUtil.exportListToExcel(fileName, exportList, StudentExportDTO.class);
        noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
    }


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
        packageGradeInfo(grades);

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
        if (CollectionUtils.isEmpty(schools)) {
            throw new BusinessException("学校编号异常");
        }

        if (idCards.stream().distinct().count() < idCards.size()) {
            throw new BusinessException("学生身份证号码重复");
        }

        if (studentService.checkIdCards(idCards)) {
            throw new BusinessException("学生身份证号码重复");
        }
    }

    /**
     * 封装年级班级信息
     *
     * @param grades 年级列表
     */
    private void packageGradeInfo(List<SchoolGradeExportDTO> grades) {
        List<Integer> gradeIds = grades.stream().map(SchoolGradeExportDTO::getId).collect(Collectors.toList());
        // 班级统计
        List<SchoolClassExportDTO> classes = schoolClassService.getByGradeIds(gradeIds);
        // 通过班级id分组
        Map<Integer, List<SchoolClassExportDTO>> classMaps = classes.stream().collect(Collectors.groupingBy(SchoolClassExportDTO::getGradeId));
        // 年级设置班级
        grades.forEach(g -> g.setChild(classMaps.get(g.getId())));
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
        if (!CollectionUtils.isEmpty(checkIdCards)) {
            throw new BusinessException("身份证号码已经被使用，请确认！");
        }

        // 收集手机号码
        List<String> phones = listMap.stream().map(s -> s.get(3)).collect(Collectors.toList());
        if (phones.stream().distinct().count() < phones.size()) {
            throw new BusinessException("手机号码重复");
        }

        List<User> checkPhones = oauthServiceClient.getUserBatchByPhones(phones, SystemCode.SCREENING_CLIENT.getCode());
        if (!CollectionUtils.isEmpty(checkPhones)) {
            throw new BusinessException("手机号码已经被使用，请确认！");
        }
    }

    /**
     * 检查筛查人员信息
     *
     * @param item 筛查人员
     */
    private void checkStaffInfo(Map<Integer, String> item) {
        if (StringUtils.isBlank(item.get(1)) || GenderEnum.getType(item.get(1)).equals(0)) {
            throw new BusinessException("性别异常");
        }

        if (StringUtils.isBlank(item.get(2)) || !Pattern.matches(RegularUtils.REGULAR_ID_CARD, item.get(2))) {
            throw new BusinessException("身份证异常");
        }

        if (StringUtils.isBlank(item.get(3)) || !Pattern.matches(RegularUtils.REGULAR_MOBILE, item.get(3))) {
            throw new BusinessException("手机号码异常");
        }
    }

    /**
     * 获取学生的导入模版
     */
    public File getStudentImportDemo() throws IOException {
        ClassPathResource resource = new ClassPathResource("excel" + File.separator + "ImportStudentTemplate.xlsx");
        InputStream inputStream = resource.getInputStream();
        File templateFile = File.createTempFile("ImportStudentTemplate", FILE_SUFFIX);
        try {
            FileUtils.copyInputStreamToFile(inputStream, templateFile);
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(inputStream);
        }
        return templateFile;
    }

    /**
     * 获取筛查机构人员的导入模版
     */
    public File getScreeningOrganizationStaffImportDemo() throws IOException {
        ClassPathResource resource = new ClassPathResource("excel" + File.separator + "ImportStaffTemplate.xlsx");
        // 获取文件
        InputStream inputStream = resource.getInputStream();
        File templateFile = File.createTempFile("ImportStaffTemplate", FILE_SUFFIX);
        try {
            FileUtils.copyInputStreamToFile(inputStream, templateFile);
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(inputStream);
        }
        return templateFile;
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
        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS + "统计报表", "数据对比表", new Date());
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
            exportVo.setId(i + 1).setGenderDesc(GenderEnum.getName(vo.getGender())).setNationDesc(StringUtils.defaultString(NationEnum.getName(vo.getNation())))
                    .setGlassesTypeDesc(Objects.isNull(glassesType) ? "--" : glassesType.desc).setIsRescreenDesc("否")
                    .setWarningLevelDesc(StringUtils.defaultIfBlank(WarningLevel.getDesc(vo.getWarningLevel()), "--"));
            genScreeningData(vo, exportVo);
            genReScreeningData(rescreenPlanStudentIdVoMap, vo, exportVo);
            exportVos.add(exportVo);
        }
        return exportVos;
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
     * 组装初筛数据
     *
     * @param dto
     * @param exportDTO
     */
    private void genScreeningData(StatConclusionExportDTO dto, VisionScreeningResultExportDTO exportDTO) {
        exportDTO.setNakedVisions(eyeDataFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION), 1))
                .setCorrectedVisions(eyeDataFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CORRECTED_VISION), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CORRECTED_VISION), 1))
                .setSphs(eyeDataFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_SPH), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_SPH), 2))
                .setCyls(eyeDataFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CYL), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CYL), 2))
                .setAxials(eyeDataFormat((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_AXIAL), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_AXIAL), 0))
                .setSphericalEquivalents(eyeDataFormat(StatUtil.getSphericalEquivalent((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_SPH), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.RIGHTEYE_CYL)), StatUtil.getSphericalEquivalent((BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_SPH), (BigDecimal) JSONPath.eval(dto, ScreeningResultPahtConst.LEFTEYE_CYL)), 2));
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
     * 根据id批量获取用户
     *
     * @param userIds 用户id列
     * @return Map<用户id ， 用户>
     */
    private Map<Integer, User> getUserMapByIds(Set<Integer> userIds) {
        return oauthServiceClient.getUserBatchByIds(new ArrayList<>(userIds)).stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    /**
     * 检查行政区域id
     *
     * @param districtId 行政区域
     * @return District 行政区域
     */
    private District checkAndGetDistrict(Integer districtId) {
        if (Objects.isNull(districtId)) {
            throw new BusinessException("行政区域id不能为空");
        }
        District district = null;
        try {
            district = districtService.findOne(new District().setId(districtId));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Objects.isNull(district)) {
            throw new BusinessException("未找到该行政区域");
        }
        return district;
    }

    /**
     * 获取省市区
     *
     * @param item AddressCode
     * @param <T>  实体
     * @return HashMap<String, String>
     */
    private <T extends AddressCode> HashMap<String, String> generateAddressMap(T item) {
        HashMap<String, String> addressCodeMap = new HashMap<>();
        if (Objects.nonNull(item.getProvinceCode())) {
            addressCodeMap.put(ExportAddressKey.PROVIDE, districtService.getDistrictName(item.getProvinceCode()));
        }
        if (Objects.nonNull(item.getCityCode())) {
            addressCodeMap.put(ExportAddressKey.CITY, districtService.getDistrictName(item.getCityCode()));
        }
        if (Objects.nonNull(item.getAreaCode())) {
            addressCodeMap.put(ExportAddressKey.AREA, districtService.getDistrictName(item.getAreaCode()));
        }
        if (Objects.nonNull(item.getTownCode())) {
            addressCodeMap.put(ExportAddressKey.TOWN, districtService.getDistrictName(item.getTownCode()));
        }
        return addressCodeMap;
    }

    /**
     * 检查学生信息是否完整
     *
     * @param item 学生信息
     */
    private void checkStudentInfo(Map<Integer, String> item) {
        if (StringUtils.isBlank(item.get(1)) || GenderEnum.getType(item.get(1)).equals(-1)) {
            throw new BusinessException("学生性别异常");
        }

        if (StringUtils.isBlank(item.get(2))) {
            throw new BusinessException("学生出生日期不能为空");
        }

        if (StringUtils.isBlank(item.get(4))) {
            throw new BusinessException("学校编号不能为空");
        }

        if (StringUtils.isBlank(item.get(5))) {
            throw new BusinessException("学生年级不能为空");
        }

        if (StringUtils.isBlank(item.get(6))) {
            throw new BusinessException("学生班级不能为空");
        }

        if (StringUtils.isBlank(item.get(7))) {
            throw new BusinessException("学生学号异常");
        }

        if (StringUtils.isBlank(item.get(8)) || !Pattern.matches(RegularUtils.REGULAR_ID_CARD, item.get(8))) {
            throw new BusinessException("学生身份证异常");
        }

        if (StringUtils.isNotBlank(item.get(9)) && !Pattern.matches(RegularUtils.REGULAR_MOBILE, item.get(9))) {
            throw new BusinessException("学生手机号码异常");
        }
    }
}
