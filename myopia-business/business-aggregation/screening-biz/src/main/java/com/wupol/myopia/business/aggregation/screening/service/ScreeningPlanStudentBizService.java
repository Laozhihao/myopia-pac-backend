package com.wupol.myopia.business.aggregation.screening.service;

import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.domain.vo.PdfGeneratorVO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.business.aggregation.screening.domain.dto.UpdatePlanStudentRequestDTO;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
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

    /**
     * 筛查通知结果页面地址
     */
    public static final String SCREENING_NOTICE_RESULT_HTML_URL = "%s?planId=%d&schoolId=%s&gradeId=%s&classId=%s&orgId=%s&planStudentIdStr=%s&isSchoolClient=%s&noticeReport=1";

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
    public void asyncGeneratorPDF(Integer planId, Integer schoolId, Integer gradeId, Integer classId,
                                  Integer orgId, String planStudentIdStr, Boolean isSchoolClient, Integer userId) {
        String uuid = UUID.randomUUID().toString();
        String fileName = getFileName(schoolId, gradeId);
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
        log.info("导出URL:{}", screeningNoticeResultHtmlUrl);
        PdfResponseDTO responseDTO = html2PdfService.asyncGeneratorPDF(screeningNoticeResultHtmlUrl, fileName, uuid);
        if (responseDTO.getStatus().equals(false)) {
            // 错误删除时候删除缓存信息
            redisUtil.del(uuid);
            throw new BusinessException("异步导出学生报告异常");
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
}
