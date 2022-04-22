package com.wupol.myopia.business.aggregation.screening.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.domain.vo.PdfGeneratorVO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.business.aggregation.screening.domain.dto.UpdatePlanStudentRequestDTO;
import com.wupol.myopia.business.aggregation.screening.handler.CredentialModificationHandler;
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
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Log4j2
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
    @Autowired
    private RedisUtil redisUtil;
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
        if (!CollectionUtils.isEmpty(screeningPlanSchoolStudentService.getByIdCardAndPassport(requestDTO.getIdCard(), requestDTO.getPassport(), requestDTO.getPlanStudentId()))) {
            throw new BusinessException("身份证或护照重复，请检查");
        }
        // 获取计划学生
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = requestDTO.handlePlanStudentData(screeningPlanSchoolStudentService.getById(requestDTO.getPlanStudentId()));
        // 检查学号是否重复
        checkStudentSno(screeningPlanSchoolStudent);
        // 按证件号的变化来变更
        credentialModificationHandler.updateStudentByCredentialNO(requestDTO,screeningPlanSchoolStudent);
    }


    /**
     * 通过条件获取筛查学生
     *
     * @param planId           计划Id
     * @param schoolId         学校Id
     * @param gradeId          年级Id
     * @param classId          班级Id
     * @param orgId            筛查机构Id
     * @param planStudentIdStr 筛查学生Ids
     * @param isSchoolClient   是否学校端
     * @param planStudentName  学生名称
     * @return List<ScreeningStudentDTO>
     */
    public List<ScreeningStudentDTO> getScreeningNoticeResultStudent(Integer planId, Integer schoolId, Integer gradeId,
                                                                     Integer classId, Integer orgId, String planStudentIdStr,
                                                                     Boolean isSchoolClient, String planStudentName) {
        ResultNoticeConfig resultNoticeConfig;
        if (isSchoolClient) {
            resultNoticeConfig = schoolService.getBySchoolId(schoolId).getResultNoticeConfig();
        } else {
            resultNoticeConfig = screeningOrganizationService.getScreeningOrgDetails(orgId).getResultNoticeConfig();
        }
        String fileUrl = StringUtils.EMPTY;
        if (Objects.nonNull(resultNoticeConfig) && Objects.nonNull(resultNoticeConfig.getQrCodeFileId())) {
            fileUrl = resourceFileService.getResourcePath(resultNoticeConfig.getQrCodeFileId());
        }
        List<ScreeningStudentDTO> planStudents = getScreeningStudentDTOS(planId, schoolId, gradeId, classId, planStudentIdStr, planStudentName);
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
     * @param planId           计划Id
     * @param schoolId         学校Id
     * @param gradeId          年级Id
     * @param classId          班级Id
     * @param orgId            筛查机构Id
     * @param planStudentIdStr 筛查学生Ids
     * @param userId           用户Id
     */
    @Async
    public void asyncGeneratorPDF(Integer planId, Integer schoolId, Integer gradeId, Integer classId,
                                  Integer orgId, String planStudentIdStr, Boolean isSchoolClient, Integer userId) {

        List<ScreeningStudentDTO> screeningStudentDTOS = getScreeningNoticeResultStudent(planId, schoolId, gradeId, classId, orgId, planStudentIdStr, isSchoolClient, null);
        if (CollectionUtils.isEmpty(screeningStudentDTOS)) {
            return;
        }
        String fileSaveParentPath = getFileSaveParentPath() + UUID.randomUUID() + "/";

        List<Integer> schoolIds = screeningStudentDTOS.stream().map(ScreeningStudentDTO::getSchoolId).collect(Collectors.toList());
        Map<Integer, String> schoolMap = schoolService.getByIds(schoolIds).stream().collect(Collectors.toMap(School::getId, School::getName));

        List<Integer> gradeIds = screeningStudentDTOS.stream().map(ScreeningStudentDTO::getGradeId).collect(Collectors.toList());
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(gradeIds);

        List<Integer> classIds = screeningStudentDTOS.stream().map(ScreeningStudentDTO::getClassId).collect(Collectors.toList());
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(classIds);

        Map<Integer, List<ScreeningStudentDTO>> planGroup = screeningStudentDTOS.stream().collect(Collectors.groupingBy(ScreeningStudentDTO::getPlanId));

        String appendName = getAppendName(schoolId, orgId, isSchoolClient);
        for (Map.Entry<Integer, List<ScreeningStudentDTO>> planEntry : planGroup.entrySet()) {
            List<ScreeningStudentDTO> planList = planEntry.getValue();
            if (CollectionUtils.isEmpty(planList)) {
                continue;
            }
            Map<Integer, List<ScreeningStudentDTO>> schoolGroup = planList.stream().collect(Collectors.groupingBy(ScreeningStudentDTO::getSchoolId));
            for (Map.Entry<Integer, List<ScreeningStudentDTO>> schoolEntry : schoolGroup.entrySet()) {
                List<ScreeningStudentDTO> schoolList = schoolEntry.getValue();
                if (CollectionUtils.isEmpty(schoolList)) {
                    continue;
                }
                Map<Integer, List<ScreeningStudentDTO>> gradeGroup = schoolList.stream().collect(Collectors.groupingBy(StudentDO::getGradeId));
                for (Map.Entry<Integer, List<ScreeningStudentDTO>> gradeEntry : gradeGroup.entrySet()) {
                    List<ScreeningStudentDTO> gradeList = gradeEntry.getValue();
                    if (CollectionUtils.isEmpty(gradeList)) {
                        continue;
                    }
                    Map<Integer, List<ScreeningStudentDTO>> classGroup = gradeList.stream().collect(Collectors.groupingBy(StudentDO::getClassId));
                    if (CollectionUtils.isEmpty(classGroup)) {
                        continue;
                    }
                    for (Map.Entry<Integer, List<ScreeningStudentDTO>> classEntry : classGroup.entrySet()) {
                        List<ScreeningStudentDTO> classList = classEntry.getValue();
                        if (CollectionUtils.isEmpty(classList)) {
                            continue;
                        }
                        String screeningNoticeResultHtmlUrl = String.format(SCREENING_NOTICE_RESULT_HTML_URL,
                                htmlUrlHost,
                                planEntry.getKey(),
                                Objects.nonNull(schoolEntry.getKey()) ? schoolEntry.getKey() : StringUtils.EMPTY,
                                Objects.nonNull(gradeEntry.getKey()) ? gradeEntry.getKey() : StringUtils.EMPTY,
                                Objects.nonNull(classEntry.getKey()) ? classEntry.getKey() : StringUtils.EMPTY,
                                Objects.nonNull(orgId) ? orgId : StringUtils.EMPTY,
                                Objects.nonNull(planStudentIdStr) ? planStudentIdStr : StringUtils.EMPTY,
                                isSchoolClient);
                        String uuid = UUID.randomUUID().toString();
                        String fileName = SCREENING_NAME;
                        PdfResponseDTO pdfResponseDTO = html2PdfService.syncGeneratorPDF(screeningNoticeResultHtmlUrl, fileName, uuid);
                        log.info("response:{}", JSONObject.toJSONString(pdfResponseDTO));
                        try {
                            FileUtils.downloadFile(pdfResponseDTO.getUrl(),
                                    fileSaveParentPath +
                                            schoolMap.get(schoolEntry.getKey()) + SCREENING_NAME + "/" +
                                            gradeMap.get(gradeEntry.getKey()).getName() + SCREENING_NAME + "/" +
                                            classMap.get(classEntry.getKey()).getName() + SCREENING_NAME + "/" +
                                            fileName + ".pdf");
                        } catch (Exception e) {
                            log.error("Exception", e);
                        }
                    }
                }
            }
        }
        File renameFile = FileUtil.rename(ZipUtil.zip(fileSaveParentPath), appendName + SCREENING_NAME + ".zip", true);
        try {
            noticeService.sendExportSuccessNotice(userId, userId, appendName + SCREENING_NAME, s3Utils.uploadFileToS3(renameFile));
        } catch (UtilException e) {
            noticeService.sendExportFailNotice(userId, userId, appendName + SCREENING_NAME);
            throw new BusinessException("发送通知异常");
        } finally {
            FileUtil.del(fileSaveParentPath);
        }
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
     * @param planId           计划Id
     * @param schoolId         学校Id
     * @param gradeId          年级Id
     * @param classId          班级Id
     * @param orgId            筛查机构Id
     * @param planStudentIdStr 筛查学生Ids
     * @param userId           用户Id
     */
    public PdfResponseDTO syncGeneratorPDF(Integer planId, Integer schoolId, Integer gradeId, Integer classId,
                                           Integer orgId, String planStudentIdStr, Boolean isSchoolClient, Integer userId) {

        // 检查学生是否有筛查数据
        if (StringUtils.isNotBlank(planStudentIdStr)) {
            List<Integer> planStudentId = ListUtil.str2List(planStudentIdStr);
            List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByPlanStudentIds(planStudentId);
            if (CollectionUtils.isEmpty(visionScreeningResults)) {
                throw new BusinessException("学生无筛查数据，操作失败！");
            }
        }
        String fileName = getFileName(schoolId, gradeId);
        String uuid = UUID.randomUUID().toString();
        cacheInfo(uuid, userId, fileName);
        String screeningNoticeResultHtmlUrl = String.format(SCREENING_NOTICE_RESULT_HTML_URL,
                htmlUrlHost,
                planId,
                Objects.nonNull(schoolId) ? schoolId : StringUtils.EMPTY,
                Objects.nonNull(gradeId) ? gradeId : StringUtils.EMPTY,
                Objects.nonNull(classId) ? classId : StringUtils.EMPTY,
                Objects.nonNull(orgId) ? orgId : StringUtils.EMPTY,
                Objects.nonNull(planStudentIdStr) ? planStudentIdStr : StringUtils.EMPTY,
                isSchoolClient);
        return html2PdfService.syncGeneratorPDF(screeningNoticeResultHtmlUrl, fileName, uuid);
    }

    /**
     * 缓存导出信息
     *
     * @param uuid     UUID
     * @param userId   用户Id
     * @param fileName 文件名
     */
    private void cacheInfo(String uuid, Integer userId, String fileName) {
        PdfGeneratorVO pdfGeneratorVO = new PdfGeneratorVO(userId, fileName);
        redisUtil.set(uuid, pdfGeneratorVO, 60 * 60 * 12);
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
     * @param planId           计划Id
     * @param schoolId         学校Id
     * @param gradeId          年级Id
     * @param classId          班级Id
     * @param planStudentIdStr 筛查学生Ids
     * @param planStudentName  学生名称
     * @return List<ScreeningStudentDTO>
     */
    public List<ScreeningStudentDTO> getScreeningStudentDTOS(Integer planId, Integer schoolId, Integer gradeId, Integer classId, String planStudentIdStr, String planStudentName) {
        List<Integer> planStudentId = ListUtil.str2List(planStudentIdStr);
        return screeningPlanSchoolStudentService.getScreeningNoticeResultStudent(Collections.singletonList(planId), schoolId, gradeId, classId, CollectionUtils.isEmpty(planStudentId) ? null : planStudentId, planStudentName);
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
        List<ScreeningPlanSchoolStudent> planStudents = screeningPlanSchoolStudentService.getByIds(planStudentIds);
        if (!CollectionUtils.isEmpty(planStudents)) {
            DeletedArchive deletedArchive = new DeletedArchive();
            deletedArchive.setType(DeletedArchive.PLAN_STUDENT_TYPE);
            deletedArchive.setContent(JSONObject.toJSONString(planStudents));
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
        Page<ScreeningStudentDTO> page = (Page<ScreeningStudentDTO>) pageRequest.toPage();
        ScreeningStudentQueryDTO screeningStudentQueryDTO = ScreeningStudentQueryDTO.getScreeningStudentQueryDTO(mockPlanStudentQueryDTO);
        IPage<ScreeningStudentDTO> screeningPlanIPage = screeningPlanSchoolStudentService.selectPageByQuery(page, screeningStudentQueryDTO);
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
        Map<Integer, VisionScreeningResult> planStudentVisionResultMap = resultList.stream().collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));

        Set<Integer> orgIdSet = screeningStudentDTOS.stream().map(ScreeningStudentDTO::getScreeningOrgId).collect(Collectors.toSet());
        Map<Integer, String> orgIdMap = screeningOrganizationService.getByIds(orgIdSet).stream().collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getName, (v1, v2) -> v2));

        for (ScreeningStudentDTO studentDTO : screeningStudentDTOS) {
            studentDTO.setNationDesc(NationEnum.getName(studentDTO.getNation()))
                    .setAddress(districtService.getAddressDetails(studentDTO.getProvinceCode(), studentDTO.getCityCode(), studentDTO.getAreaCode(), studentDTO.getTownCode(), studentDTO.getAddress()));
            studentDTO.setScreeningOrgName(orgIdMap.get(studentDTO.getScreeningOrgId()));
            setStudentEyeInfo(studentDTO, planStudentVisionResultMap);
        }
        return screeningStudentDTOS;
    }

    /**
     * 获取计划学生
     *
     * @return
     */
    private Set<Integer> getPlanIds(MockPlanStudentQueryDTO mockPlanStudentQueryDTO) {
        Set<Integer> orgIds = null;
        if (StringUtils.isNotBlank(mockPlanStudentQueryDTO.getScreeningOrgNameLike())) {
            List<ScreeningOrganization> screeningOrganizations = screeningOrganizationService.getByNameLike(mockPlanStudentQueryDTO.getScreeningOrgNameLike());
            Map<Integer, String> orgIdMap = screeningOrganizations.stream().collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getName, (v1, v2) -> v2));
            orgIds = orgIdMap.keySet();
            if (CollectionUtils.isEmpty(orgIdMap)) {
                // 可以直接返回空
                return null;
            }
        }
        //02.根据orgIds查找筛查计划信息
        LambdaQueryWrapper<ScreeningPlan> screeningPlanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(orgIds)) {
            //如果是空的话, 说明没有搜索orgIds的情况
            screeningPlanLambdaQueryWrapper.in(ScreeningPlan::getScreeningOrgId, orgIds);
        }
        if (StringUtils.isNotBlank(screeningPlanLambdaQueryWrapper.getCustomSqlSegment())) {
            List<ScreeningPlan> screeningPlans = screeningPlanService.getBaseMapper().selectList(screeningPlanLambdaQueryWrapper);
            return screeningPlans.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        }
        return null;
    }


    /**
     * @Description: 给学生扩展类赋值
     * @Param: [studentEyeInfor]
     * @return: void
     * @Author: 钓猫的小鱼
     * @Date: 2022/1/5
     */
    public void setStudentEyeInfo(ScreeningStudentDTO studentEyeInfo, Map<Integer, VisionScreeningResult> visionScreeningResultsMap) {
        VisionScreeningResult visionScreeningResult = null;
        if (!CollectionUtils.isEmpty(visionScreeningResultsMap)) {
            visionScreeningResult = visionScreeningResultsMap.get(studentEyeInfo.getPlanStudentId());
        }
        studentEyeInfo.setHasScreening(Objects.nonNull(visionScreeningResult));
        //是否戴镜情况
        studentEyeInfo.setGlassesTypeDes(EyeDataUtil.glassesTypeString(visionScreeningResult));

        //裸视力
        String nakedVision = EyeDataUtil.visionRightDataToStr(visionScreeningResult) + "/" + EyeDataUtil.visionLeftDataToStr(visionScreeningResult);
        studentEyeInfo.setNakedVision(nakedVision);
        //矫正 视力
        String correctedVision = EyeDataUtil.correctedRightDataToStr(visionScreeningResult) + "/" + EyeDataUtil.correctedLeftDataToStr(visionScreeningResult);
        studentEyeInfo.setCorrectedVision(correctedVision);
        //球镜
        studentEyeInfo.setRSph(EyeDataUtil.computerRightSphNULL(visionScreeningResult));
        studentEyeInfo.setLSph(EyeDataUtil.computerLeftSphNull(visionScreeningResult));
        //柱镜
        studentEyeInfo.setRCyl(EyeDataUtil.computerRightCylNull(visionScreeningResult));
        studentEyeInfo.setLCyl(EyeDataUtil.computerLeftCylNull(visionScreeningResult));
        //眼轴
        String axial = EyeDataUtil.computerRightAxial(visionScreeningResult) + "/" + EyeDataUtil.computerLeftAxial(visionScreeningResult);
        studentEyeInfo.setAxial(axial);

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
}
