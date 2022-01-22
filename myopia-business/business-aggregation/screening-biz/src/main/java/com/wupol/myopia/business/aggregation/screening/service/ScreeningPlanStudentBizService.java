package com.wupol.myopia.business.aggregation.screening.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.domain.vo.PdfGeneratorVO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.business.aggregation.screening.domain.dto.UpdatePlanStudentRequestDTO;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.StudentDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vistel.Interface.exception.UtilException;
import org.springframework.util.CollectionUtils;


import javax.annotation.Resource;
import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;
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
    @Autowired
    private StudentService studentService;
    @Resource
    private SchoolStudentService schoolStudentService;
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
    @Value("${report.pdf.save-path}")
    public String pdfSavePath;
    @Resource
    private NoticeService noticeService;
    @Resource
    private S3Utils s3Utils;
    @Resource
    private ScreeningPlanService screeningPlanService;
    @Resource
    private SchoolClassService schoolClassService;

    /**
     * 筛查通知结果页面地址
     */
    public static final String SCREENING_NOTICE_RESULT_HTML_URL = "%s?planId=%d&schoolId=%s&gradeId=%s&classId=%s&orgId=%s&planStudentIdStr=%s&isSchoolClient=%s&noticeReport=1";

    private static final String SCREENING_NAME = "筛查结构通知书";

    /**
     * 更新筛查学生
     *
     * @param requestDTO 更新学生入参
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePlanStudent(UpdatePlanStudentRequestDTO requestDTO) {
        // 更新计划学生信息
        ScreeningPlanSchoolStudent planSchoolStudent = screeningPlanSchoolStudentService.getById(requestDTO.getPlanStudentId());
        planSchoolStudent.setStudentName(requestDTO.getName());
        planSchoolStudent.setGender(requestDTO.getGender());
        planSchoolStudent.setStudentAge(requestDTO.getStudentAge());
        planSchoolStudent.setBirthday(requestDTO.getBirthday());
        if (StringUtils.isNotBlank(requestDTO.getParentPhone())) {
            planSchoolStudent.setParentPhone(requestDTO.getParentPhone());
        }
        if (StringUtils.isNotBlank(requestDTO.getSno())) {
            planSchoolStudent.setStudentNo(requestDTO.getSno());
        }
        screeningPlanSchoolStudentService.updateById(planSchoolStudent);
        // 更新原始学生信息
        Integer studentId = planSchoolStudent.getStudentId();
        Student student = studentService.getById(studentId);
        student.setName(requestDTO.getName());
        student.setGender(requestDTO.getGender());
        student.setBirthday(requestDTO.getBirthday());
        if (StringUtils.isNotBlank(requestDTO.getParentPhone())) {
            student.setParentPhone(requestDTO.getParentPhone());
        }
        if (StringUtils.isNotBlank(requestDTO.getSno())) {
            student.setSno(requestDTO.getSno());
        }
        studentService.updateById(student);

        // 更新学校端学生
        List<SchoolStudent> schoolStudents = schoolStudentService.getByStudentId(studentId);
        if (CollectionUtils.isEmpty(schoolStudents)) {
            return;
        }
        schoolStudents.forEach(schoolStudent -> {
            schoolStudent.setName(requestDTO.getName());
            schoolStudent.setGender(requestDTO.getGender());
            schoolStudent.setBirthday(requestDTO.getBirthday());
            if (StringUtils.isNotBlank(requestDTO.getParentPhone())) {
                schoolStudent.setParentPhone(requestDTO.getParentPhone());
            }
            if (StringUtils.isNotBlank(requestDTO.getSno())) {
                schoolStudent.setSno(requestDTO.getSno());
            }
        });
        schoolStudentService.updateBatchById(schoolStudents);
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

        String appendName;
        if (isSchoolClient) {
            appendName = schoolService.getById(schoolId).getName();
        } else {
            appendName = screeningOrganizationService.getById(orgId).getName();
        }

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
                            downloadFile(pdfResponseDTO.getUrl(),
                                    fileSaveParentPath +
                                            schoolMap.get(schoolEntry.getKey()) + SCREENING_NAME + "/" +
                                            gradeMap.get(gradeEntry.getKey()).getName() + SCREENING_NAME + "/" +
                                            classMap.get(classEntry.getKey()).getName() + SCREENING_NAME + "/" +
                                            fileName + ".pdf");
                        } catch (Exception e) {
                            log.error("Exception",e);
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
        return screeningPlanSchoolStudentService.getScreeningNoticeResultStudent(planId, schoolId, gradeId, classId, CollectionUtils.isEmpty(planStudentId) ? null : planStudentId, planStudentName);
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
     * 文件下载
     * @param fileUrl 下载路径
     * @param savePath 存放地址
     */
    public static void downloadFile(String fileUrl,String savePath) throws Exception {

        File file = new File(savePath);
        File parentFile = file.getParentFile();
        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }

        URL url = new URL(fileUrl);
        URLConnection conn = url.openConnection();
        InputStream inputStream = conn.getInputStream();
        FileOutputStream fileOutputStream = new FileOutputStream(savePath);
        int byteRead;
        byte[] buffer = new byte[1024];
        while ((byteRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, byteRead);
        }
        fileOutputStream.close();
    }
}
