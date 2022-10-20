package com.wupol.myopia.business.aggregation.screening.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.business.aggregation.screening.domain.bo.GeneratorPdfBO;
import com.wupol.myopia.business.aggregation.screening.domain.dto.GeneratorPdfDTO;
import com.wupol.myopia.business.aggregation.screening.domain.dto.UpdatePlanStudentRequestDTO;
import com.wupol.myopia.business.aggregation.screening.handler.CredentialModificationHandler;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.common.domain.model.DeletedArchive;
import com.wupol.myopia.business.core.common.service.DeletedArchiveService;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.school.domain.dto.MockPlanStudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.StudentDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/9/16
 **/
@Service
@Slf4j
public class ScreeningPlanStudentBizService {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private SchoolService schoolService;
    @Resource
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private Html2PdfService html2PdfService;
    @Resource
    private SchoolGradeService schoolGradeService;
    @Resource
    private ResourceFileService resourceFileService;
    @Resource
    private VisionScreeningResultService visionScreeningResultService;
    @Value("${file.temp.save-path}")
    public String pdfSavePath;
    @Resource
    private NoticeService noticeService;
    @Resource
    private S3Utils s3Utils;
    @Resource
    private SchoolClassService schoolClassService;
    @Resource
    private ScreeningPlanService screeningPlanService;
    @Resource
    private DistrictService districtService;
    @Resource
    private CredentialModificationHandler credentialModificationHandler;
    @Resource
    private StudentService studentService;
    @Resource
    private DeletedArchiveService deletedArchiveService;
    @Autowired
    private ScreeningPlanSchoolStudentFacadeService screeningPlanSchoolStudentFacadeService;


    /**
     * 筛查通知结果页面地址
     */
    public static final String SCREENING_NOTICE_RESULT_HTML_URL = "%s?planId=%d&schoolId=%s&gradeId=%s&classId=%s&orgId=%s&planStudentIdStr=%s&isSchoolClient=%s&noticeReport=1";

    private static final String SCREENING_NAME = "筛查结果通知书";

    /**
     * 更新筛查学生
     *
     * @param requestDTO 更新学生入参
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePlanStudent(UpdatePlanStudentRequestDTO requestDTO) {
        requestDTO.checkStudentInfo();
        // 身份证或护照是否在同一计划下已经绑定了数据
        Integer planStudentId = requestDTO.getPlanStudentId();
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(planStudentId);
        ScreeningPlan plan = screeningPlanService.getById(planStudent.getScreeningPlanId());
        Assert.isTrue(CommonConst.STATUS_RELEASE.equals(plan.getReleaseStatus()), "学生所属筛查计划已作废！");
        if (!CollectionUtils.isEmpty(screeningPlanSchoolStudentService.getByPlanIdIdCardAndPassport(planStudent.getScreeningPlanId(), requestDTO.getIdCard(), requestDTO.getPassport(), planStudentId))) {
            throw new BusinessException("身份证或护照重复，请检查");
        }
        // 获取计划学生
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = requestDTO.handlePlanStudentData(planStudent);
        // 检查学号是否重复
        checkStudentSno(screeningPlanSchoolStudent);
        // 按证件号的变化来变更
        credentialModificationHandler.updateStudentByCredentialNO(requestDTO,screeningPlanSchoolStudent);
    }


    /**
     * 通过条件获取筛查学生
     *
     * @param generatorPdfDTO   导出条件
     * @return List<ScreeningStudentDTO>
     */
    public List<ScreeningStudentDTO> getScreeningNoticeResultStudent(GeneratorPdfDTO generatorPdfDTO) {
        ResultNoticeConfig resultNoticeConfig;
        if (Objects.equals(generatorPdfDTO.getIsSchoolClient(),Boolean.TRUE)) {
            resultNoticeConfig = schoolService.getBySchoolId(generatorPdfDTO.getSchoolId()).getResultNoticeConfig();
        } else {
            resultNoticeConfig = screeningOrganizationService.getScreeningOrgDetails(generatorPdfDTO.getOrgId()).getResultNoticeConfig();
        }
        String fileUrl = StringUtils.EMPTY;
        if (Objects.nonNull(resultNoticeConfig) && Objects.nonNull(resultNoticeConfig.getQrCodeFileId())) {
            fileUrl = resourceFileService.getResourcePath(resultNoticeConfig.getQrCodeFileId());
        }
        List<ScreeningStudentDTO> planStudents = getScreeningStudentDTOS(generatorPdfDTO);
        for (ScreeningStudentDTO planStudent : planStudents) {
            planStudent.setResultNoticeConfig(resultNoticeConfig);
            planStudent.setNoticeQrCodeFileUrl(fileUrl);
        }
        // 获取筛查学生
        List<Integer> planStudentIds = planStudents.stream().map(ScreeningStudentDTO::getPlanStudentId).collect(Collectors.toList());
        // 过滤没有筛查数据的学生
        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanStudentIds(planStudentIds);
        if (CollectionUtils.isEmpty(screeningResults)) {
            return new ArrayList<>();
        }
        Map<Integer, List<VisionScreeningResult>> visionResultMap = screeningResults.stream().collect(Collectors.groupingBy(VisionScreeningResult::getScreeningPlanSchoolStudentId));
        return planStudents.stream().filter(s -> Objects.nonNull(visionResultMap.get(s.getPlanStudentId()))).collect(Collectors.toList());
    }

    /**
     * 异步导出学生报告
     *
     * @param generatorPdfDTO   导出条件
     */
    @Async
    public void asyncGeneratorPDF(GeneratorPdfDTO generatorPdfDTO) {

        List<ScreeningStudentDTO> screeningStudentDTOS = getScreeningNoticeResultStudent(generatorPdfDTO);
        if (CollectionUtils.isEmpty(screeningStudentDTOS)) {
            return;
        }
        String fileSaveParentPath = getFileSaveParentPath() + UUID.randomUUID() + StrUtil.SLASH;

        List<Integer> schoolIds = screeningStudentDTOS.stream().map(ScreeningStudentDTO::getSchoolId).collect(Collectors.toList());
        Map<Integer, String> schoolMap = schoolService.getByIds(schoolIds).stream().collect(Collectors.toMap(School::getId, School::getName));

        List<Integer> gradeIds = screeningStudentDTOS.stream().map(ScreeningStudentDTO::getGradeId).collect(Collectors.toList());
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(gradeIds);

        List<Integer> classIds = screeningStudentDTOS.stream().map(ScreeningStudentDTO::getClassId).collect(Collectors.toList());
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(classIds);

        Map<Integer, List<ScreeningStudentDTO>> planGroup = screeningStudentDTOS.stream().collect(Collectors.groupingBy(ScreeningStudentDTO::getPlanId));

        String appendName = getAppendName(generatorPdfDTO.getSchoolId(), generatorPdfDTO.getOrgId(), generatorPdfDTO.getIsSchoolClient());
        GeneratorPdfBO generatorPdfBO = new GeneratorPdfBO()
                .setOrgId(generatorPdfDTO.getOrgId()).setPlanStudentIdStr(generatorPdfDTO.getPlanStudentIdStr())
                .setIsSchoolClient(generatorPdfDTO.getIsSchoolClient()).setFileSaveParentPath(fileSaveParentPath)
                .setSchoolMap(schoolMap).setGradeMap(gradeMap).setClassMap(classMap).setPlanGroup(planGroup);
        planInfo(generatorPdfBO);
        File renameFile = FileUtil.rename(ZipUtil.zip(fileSaveParentPath), appendName + SCREENING_NAME + ".zip", true);
        try {
            noticeService.sendExportSuccessNotice(generatorPdfDTO.getUserId(), generatorPdfDTO.getUserId(), appendName + SCREENING_NAME, s3Utils.uploadFileToS3(renameFile));
        } catch (UtilException e) {
            noticeService.sendExportFailNotice(generatorPdfDTO.getUserId(), generatorPdfDTO.getUserId(), appendName + SCREENING_NAME);
            throw new BusinessException("发送通知异常");
        } finally {
            FileUtil.del(fileSaveParentPath);
        }
    }

    /**
     * 筛查计划信息
     *
     * @param generatorPdfBO 导出条件
     */
    private void planInfo(GeneratorPdfBO generatorPdfBO) {
        generatorPdfBO.getPlanGroup().forEach((planId,planList)->{
            if (CollUtil.isNotEmpty(planList)) {
                Map<Integer, List<ScreeningStudentDTO>> schoolGroup = planList.stream().collect(Collectors.groupingBy(ScreeningStudentDTO::getSchoolId));
                generatorPdfBO.setSchoolGroup(schoolGroup);
                generatorPdfBO.setPlanId(planId);
                schoolInfo(generatorPdfBO);
            }
        });
    }

    /**
     * 学校信息
     *
     * @param generatorPdfBO 导出条件
     */
    private void schoolInfo(GeneratorPdfBO generatorPdfBO) {
        generatorPdfBO.getSchoolGroup().forEach((schoolId,schoolList)->{
            if (CollUtil.isNotEmpty(schoolList)){
                Map<Integer, List<ScreeningStudentDTO>> gradeGroup = schoolList.stream().collect(Collectors.groupingBy(StudentDO::getGradeId));
                generatorPdfBO.setSchoolId(schoolId);
                generatorPdfBO.setGradeGroup(gradeGroup);
                gradeInfo(generatorPdfBO);
            }
        });
    }

    /**
     * 年级信息
     *
     * @param generatorPdfBO 导出条件
     */
    private void gradeInfo(GeneratorPdfBO generatorPdfBO) {
        generatorPdfBO.getGradeGroup().forEach((gradeId,gradeList)->{
            Map<Integer, List<ScreeningStudentDTO>> classGroup = Maps.newHashMap();
            if (CollUtil.isNotEmpty(gradeList)) {
                classGroup = gradeList.stream().collect(Collectors.groupingBy(StudentDO::getClassId));
            }
            if (CollUtil.isNotEmpty(classGroup)) {
                generatorPdfBO.setGradeId(gradeId);
                generatorPdfBO.setClassGroup(classGroup);
                classInfo(generatorPdfBO);
            }
        });
    }

    /**
     * 班级信息
     *
     * @param generatorPdfBO 导出条件
     */
    private void classInfo(GeneratorPdfBO generatorPdfBO) {
        generatorPdfBO.getClassGroup().forEach((classId,classList)->{
            if (CollUtil.isNotEmpty(classList)) {
                generatorPdfBO.setClassId(classId);
                String screeningNoticeResultHtmlUrl = getScreeningNoticeResultHtmlUrl(generatorPdfBO);
                String fileName = SCREENING_NAME;
                PdfResponseDTO pdfResponseDTO = html2PdfService.syncGeneratorPDF(screeningNoticeResultHtmlUrl, fileName);
                log.info("response:{}", JSON.toJSONString(pdfResponseDTO));
                generatorPdfBO.setFileName(fileName);
                downloadPDFFile(generatorPdfBO, pdfResponseDTO);
            }
        });
    }

    /**
     * 下载pdf文件
     *
     * @param generatorPdfBO
     * @param pdfResponseDTO
     */
    private void downloadPDFFile(GeneratorPdfBO generatorPdfBO, PdfResponseDTO pdfResponseDTO) {
        try {
            FileUtils.downloadFile(pdfResponseDTO.getUrl(),
                    generatorPdfBO.getFileSaveParentPath() +
                            generatorPdfBO.getSchoolMap().get(generatorPdfBO.getSchoolId()) + SCREENING_NAME + "/" +
                            generatorPdfBO.getGradeMap().get(generatorPdfBO.getGradeId()).getName() + SCREENING_NAME + "/" +
                            generatorPdfBO.getClassMap().get(generatorPdfBO.getClassId()).getName() + SCREENING_NAME + "/" +
                            generatorPdfBO.getFileName() + ".pdf");
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     * 获取筛查通知结果html的url
     *
     * @param generatorPdfBO 导出条件
     */
    private String getScreeningNoticeResultHtmlUrl(GeneratorPdfBO generatorPdfBO) {
        return String.format(SCREENING_NOTICE_RESULT_HTML_URL,
                htmlUrlHost,
                generatorPdfBO.getPlanId(),
                Objects.nonNull(generatorPdfBO.getSchoolId()) ? generatorPdfBO.getSchoolId() : StringUtils.EMPTY,
                Objects.nonNull(generatorPdfBO.getGradeId()) ? generatorPdfBO.getGradeId() : StringUtils.EMPTY,
                Objects.nonNull(generatorPdfBO.getClassId()) ? generatorPdfBO.getClassId() : StringUtils.EMPTY,
                Objects.nonNull(generatorPdfBO.getOrgId()) ? generatorPdfBO.getOrgId() : StringUtils.EMPTY,
                Objects.nonNull(generatorPdfBO.getPlanStudentIdStr()) ? generatorPdfBO.getPlanStudentIdStr() : StringUtils.EMPTY,
                generatorPdfBO.getIsSchoolClient());
    }

    /**
     * 获取文件名
     *
     * @param schoolId       学校Id
     * @param orgId          机构Id
     * @param isSchoolClient 是否学校端
     * @return 文件名
     */
    private String getAppendName(Integer schoolId, Integer orgId, Boolean isSchoolClient) {
        String appendName;
        if (Boolean.TRUE.equals(isSchoolClient)) {
            appendName = schoolService.getById(schoolId).getName();
        } else {
            if (Objects.nonNull(schoolId)) {
                appendName = schoolService.getById(schoolId).getName();
            } else {
                appendName = screeningOrganizationService.getById(orgId).getName();
            }
        }
        return appendName;
    }

    /**
     * 同步导出学生报告
     *
     * @param generatorPdfDTO      导出条件
     */
    public PdfResponseDTO syncGeneratorPDF(GeneratorPdfDTO generatorPdfDTO) {

        // 检查学生是否有筛查数据
        if (StringUtils.isNotBlank(generatorPdfDTO.getPlanStudentIdStr())) {
            List<Integer> planStudentId = ListUtil.str2List(generatorPdfDTO.getPlanStudentIdStr());
            List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByPlanStudentIds(planStudentId);
            if (CollectionUtils.isEmpty(visionScreeningResults)) {
                throw new BusinessException("学生无筛查数据，操作失败！");
            }
        }
        String screeningNoticeResultHtmlUrl1 = getScreeningNoticeResultHtmlUrl(generatorPdfDTOToBo(generatorPdfDTO));
        return html2PdfService.syncGeneratorPDF(screeningNoticeResultHtmlUrl1, getFileName(generatorPdfDTO.getSchoolId(), generatorPdfDTO.getGradeId()));
    }

    /**
     * 实体转换
     * @param generatorPdfDTO
     */
    private GeneratorPdfBO generatorPdfDTOToBo(GeneratorPdfDTO generatorPdfDTO){
        return new GeneratorPdfBO()
                .setPlanId(generatorPdfDTO.getPlanId()).setSchoolId(generatorPdfDTO.getSchoolId())
                .setGradeId(generatorPdfDTO.getGradeId()).setClassId(generatorPdfDTO.getClassId())
                .setOrgId(generatorPdfDTO.getOrgId()).setPlanStudentIdStr(generatorPdfDTO.getPlanStudentIdStr())
                .setIsSchoolClient(generatorPdfDTO.getIsSchoolClient());
    }

    /**
     * 获取文件名称
     *
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @return 文件名称
     */
    private String getFileName(Integer schoolId, Integer gradeId) {
        if (Objects.nonNull(gradeId)) {
            School school = schoolService.getById(schoolId);
            SchoolGrade schoolGrade = schoolGradeService.getById(gradeId);
            return school.getName() + schoolGrade.getName() + ".pdf";
        }
        if (Objects.nonNull(schoolId)) {
            School school = schoolService.getById(schoolId);
            return school.getName() + ".pdf";
        }
        return "整个计划下的学生筛查结果通知书.pdf";
    }

    /**
     * 获取筛查学生
     *
     * @param generatorPdfDTO   导出条件
     * @return List<ScreeningStudentDTO>
     */
    public List<ScreeningStudentDTO> getScreeningStudentDTOS(GeneratorPdfDTO generatorPdfDTO) {
        List<Integer> planStudentId = ListUtil.str2List(generatorPdfDTO.getPlanStudentIdStr());

        List<ScreeningStudentDTO> screeningStudentDTOList = screeningPlanSchoolStudentService.getScreeningNoticeResultStudent(Lists.newArrayList(generatorPdfDTO.getPlanId()),generatorPdfDTO.getSchoolId() ,generatorPdfDTO.getGradeId() ,generatorPdfDTO.getClassId() , CollectionUtils.isEmpty(planStudentId) ? null : planStudentId, generatorPdfDTO.getPlanStudentName());
        if (Objects.equals(Boolean.TRUE,generatorPdfDTO.getIsData())){
            return getDataScreeningStudentDTOList(generatorPdfDTO.getPlanId(),generatorPdfDTO.getSchoolId() ,generatorPdfDTO.getGradeId(), generatorPdfDTO.getClassId(), screeningStudentDTOList);
        }
        return screeningStudentDTOList;
    }

    /**
     * 获取有数据的学生信息
     * @param planId
     * @param schoolId
     * @param gradeId
     * @param classId
     * @param screeningStudentDTOList
     */
    private List<ScreeningStudentDTO> getDataScreeningStudentDTOList(Integer planId, Integer schoolId, Integer gradeId, Integer classId, List<ScreeningStudentDTO> screeningStudentDTOList) {
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.getByPlanIdAndSchoolId(planId, schoolId);
        if (CollUtil.isEmpty(visionScreeningResultList)){
            return Lists.newArrayList();
        }
        Set<Integer> planSchoolStudentIds = visionScreeningResultList.stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toSet());
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(planSchoolStudentIds));
        Set<Integer> planStudentIds = screeningPlanSchoolStudentList.stream()
                .filter(screeningPlanSchoolStudent -> Objects.equals(screeningPlanSchoolStudent.getGradeId(), gradeId))
                .filter(screeningPlanSchoolStudent -> Objects.equals(screeningPlanSchoolStudent.getClassId(), classId))
                .map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toSet());
        return screeningStudentDTOList.stream().filter(screeningStudentDTO -> planStudentIds.contains(screeningStudentDTO.getPlanStudentId())).collect(Collectors.toList());
    }

    /**
     * 获取文件保存父目录路径
     *
     * @return java.lang.String
     **/
    public String getFileSaveParentPath() {
        return Paths.get(pdfSavePath, UUID.randomUUID().toString()).toString();
    }

    /**
     * 删除学生
     *
     * @param planStudentId 筛查学生Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletedPlanStudentById(Integer planStudentId) {
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(planStudentId);
        if (Objects.isNull(planStudent)) {
            throw new BusinessException("筛查学生异常数据异常");
        }
        VisionScreeningResult result = visionScreeningResultService.getByPlanStudentId(planStudent.getId());
        if (Objects.nonNull(result)) {
            throw new BusinessException("存在筛查记录，不能删除");
        }
        Student student = studentService.getByIdCardAndPassport(planStudent.getIdCard(), planStudent.getPassport(), null);
        if (Objects.nonNull(student)) {
            credentialModificationHandler.deletedStudent(student.getId(), student.getSchoolId(), planStudent.getScreeningPlanId());
        }
        archiveDeletedStudent(Lists.newArrayList(planStudentId));
        screeningPlanSchoolStudentService.removeById(planStudentId);
    }

    /**
     * 存档删除学生
     *
     * @param planStudentIds 学生Id
     */
    private void archiveDeletedStudent(List<Integer> planStudentIds) {
        List<ScreeningPlanSchoolStudent> planStudents = screeningPlanSchoolStudentService.listByIds(planStudentIds);
        if (!CollectionUtils.isEmpty(planStudents)) {
            DeletedArchive deletedArchive = new DeletedArchive();
            deletedArchive.setType(DeletedArchive.PLAN_STUDENT_TYPE);
            deletedArchive.setContent(JSON.toJSONString(planStudents));
            deletedArchiveService.save(deletedArchive);
        }
    }

    /**
     * 分页查询MockPlanStudent的数据
     *
     * @param pageRequest
     * @param mockPlanStudentQueryDTO
     * @return
     */
    public IPage<ScreeningStudentDTO> getMockPlanStudentList(PageRequest pageRequest, MockPlanStudentQueryDTO mockPlanStudentQueryDTO) {
        //01.根据orgName 模糊查找所有的计划id
        Set<Integer> screeningPlanIds = getPlanIds(mockPlanStudentQueryDTO);
        mockPlanStudentQueryDTO.setScreeningPlanIds(screeningPlanIds);
        //02.分页查询screeningPlanStudent表
        ScreeningStudentQueryDTO screeningStudentQueryDTO = ScreeningStudentQueryDTO.getScreeningStudentQueryDTO(mockPlanStudentQueryDTO);
        IPage<ScreeningStudentDTO> screeningPlanIPage = screeningPlanSchoolStudentService.selectPageByQuery(pageRequest.toPage(), screeningStudentQueryDTO);
        //03.补充分页后的其他数据
        List<ScreeningStudentDTO> screeningStudentDTOS = screeningPlanIPage.getRecords();
        if (CollectionUtils.isEmpty(screeningStudentDTOS)) {
            return screeningPlanIPage;
        }
        //04.补充额外信息
        supplementData(screeningStudentDTOS);
        return screeningPlanIPage;
    }

    /**
     * 补充数据
     *
     * @param screeningStudentDTOS
     * @return
     */
    private List<ScreeningStudentDTO> supplementData(List<ScreeningStudentDTO> screeningStudentDTOS) {
        if (CollectionUtils.isEmpty(screeningStudentDTOS)) {
            return screeningStudentDTOS;
        }
        List<VisionScreeningResult> resultList = visionScreeningResultService.getByPlanStudentIds(screeningStudentDTOS.stream().map(ScreeningStudentDTO::getPlanStudentId).collect(Collectors.toList()));
        Map<Integer, VisionScreeningResult> planStudentVisionResultMap = resultList.stream()
                .filter(result->Objects.equals(result.getIsDoubleScreen(),Boolean.FALSE))
                .collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));

        Set<Integer> orgIdSet = screeningStudentDTOS.stream().map(ScreeningStudentDTO::getScreeningOrgId).collect(Collectors.toSet());
        Map<Integer, String> orgIdMap = screeningOrganizationService.getByIds(orgIdSet).stream().collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getName, (v1, v2) -> v2));

        for (ScreeningStudentDTO studentDTO : screeningStudentDTOS) {
            studentDTO.setNationDesc(NationEnum.getNameByCode(studentDTO.getNation()))
                    .setAddress(districtService.getAddressDetails(studentDTO.getProvinceCode(), studentDTO.getCityCode(), studentDTO.getAreaCode(), studentDTO.getTownCode(), studentDTO.getAddress()));
            studentDTO.setScreeningOrgName(orgIdMap.get(studentDTO.getScreeningOrgId()));
            screeningPlanSchoolStudentFacadeService.setStudentEyeInfo(studentDTO, planStudentVisionResultMap);
        }
        return screeningStudentDTOS;
    }

    /**
     * 获取计划学生
     *
     * @return
     */
    private Set<Integer> getPlanIds(MockPlanStudentQueryDTO mockPlanStudentQueryDTO) {
        if (StringUtils.isBlank(mockPlanStudentQueryDTO.getScreeningOrgNameLike())) {
            return Collections.emptySet();
        }
        List<ScreeningOrganization> screeningOrganizations = screeningOrganizationService.getByNameLike(mockPlanStudentQueryDTO.getScreeningOrgNameLike(),Boolean.FALSE);
        if (CollectionUtils.isEmpty(screeningOrganizations)) {
            return Collections.emptySet();
        }
        List<Integer> orgIds = screeningOrganizations.stream().map(ScreeningOrganization::getId).collect(Collectors.toList());
        List<ScreeningPlan> screeningPlans = screeningPlanService.getReleasePlanByOrgIds(orgIds);
        return screeningPlans.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
    }

    /**
     * 检查学号是否重复
     *
     * @param screeningPlanSchoolStudent 计划学生
     */
    public void checkStudentSno(ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        List<ScreeningPlanSchoolStudent> existPlanSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlanSchoolStudent.getScreeningPlanId());
        // 检查学号
        screeningPlanSchoolStudentService.checkSno(existPlanSchoolStudentList, screeningPlanSchoolStudent.getStudentNo(), screeningPlanSchoolStudent.getIdCard(), screeningPlanSchoolStudent.getPassport(), screeningPlanSchoolStudent.getSchoolId());
    }

    /**
     * 判断筛查学生是否在筛查时间内
     *
     * @param screeningPlanSchoolStudent 筛查学生
     *
     * @return 否在筛查时间内
     */
    public boolean isNotMatchScreeningTime(ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        if (Objects.isNull(screeningPlanSchoolStudent)) {
            return true;
        }
        Integer screeningPlanId = screeningPlanSchoolStudent.getScreeningPlanId();
        ScreeningPlan plan = screeningPlanService.getById(screeningPlanId);
        Assert.isTrue(CommonConst.STATUS_RELEASE.equals(plan.getReleaseStatus()), "此二维码已作废！");
        return !DateUtil.isBetweenDate(plan.getStartTime(), plan.getEndTime());
    }

    /**
     * 更新App筛查学生
     *
     * @param requestDTO 更新学生入参
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAppPlanStudent(UpdatePlanStudentRequestDTO requestDTO) {
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(requestDTO.getPlanStudentId());
        ScreeningPlanSchoolStudent updatePlanStudent = requestDTO.handleAppPlanStudentData(planStudent);
        // 检查学号是否重复
        if (Objects.isNull(updatePlanStudent)) {
            throw new BusinessException("筛查学生数据异常!");
        }
        checkStudentSno(updatePlanStudent);
        screeningPlanSchoolStudentService.updateById(updatePlanStudent);
        // 更新多端学生
        Student student = studentService.getById(planStudent.getStudentId());
        if (Objects.isNull(student)) {
            return;
        }
        buildStudentByPlanStudent(student, planStudent);
        studentService.updateById(student);
    }

    /**
     * 通过筛查学生构建多端学生
     *
     * @param student     多端学生
     * @param planStudent 筛查学生
     */
    private void buildStudentByPlanStudent(Student student, ScreeningPlanSchoolStudent planStudent) {
        student.setName(planStudent.getStudentName());
        student.setGender(planStudent.getGender());
        student.setBirthday(planStudent.getBirthday());
        student.setSchoolId(planStudent.getSchoolId());
        student.setGradeId(planStudent.getGradeId());
        student.setClassId(planStudent.getClassId());
    }
}
