package com.wupol.myopia.business.aggregation.screening.service;

import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.service.Html2PdfService;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.business.aggregation.screening.domain.dto.UpdatePlanStudentRequestDTO;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @Author HaoHao
 * @Date 2021/9/16
 **/
@Service
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
    @Resource
    private ResourceFileService resourceFileService;
    @Autowired
    private Html2PdfService html2PdfService;

    /**
     * 筛查通知结果页面地址
     */
    public static final String SCREENING_NOTICE_RESULT_HTML_URL = "%s?planId=%d&schoolId=%d&gradeId=%d&classId=%d&planStudentId=%d&orgId=%d&isSchoolClient=%s";

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
        schoolStudents.forEach(schoolStudent->{
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
     * @param planId         计划Id
     * @param schoolId       学校Id
     * @param gradeId        年级Id
     * @param classId        班级Id
     * @param orgId          筛查机构Id
     * @param planStudentIds  筛查学生Ids
     * @param isSchoolClient 是否学校端
     * @return List<ScreeningStudentDTO>
     */
    public List<ScreeningStudentDTO> getScreeningNoticeResultStudent(Integer planId, Integer schoolId, Integer gradeId,
                                                                     Integer classId, Integer orgId,
                                                                     String planStudentIds, Boolean isSchoolClient) {
        ResultNoticeConfig resultNoticeConfig;
        if (isSchoolClient) {
            resultNoticeConfig = schoolService.getBySchoolId(schoolId).getResultNoticeConfig();
        } else {
            resultNoticeConfig = screeningOrganizationService.getScreeningOrgDetails(orgId).getResultNoticeConfig();
        }
        List<Integer> planStudentId = ListUtil.str2List(planStudentIds);
        List<ScreeningStudentDTO> planStudents = screeningPlanSchoolStudentService.getScreeningNoticeResultStudent(planId, schoolId, gradeId, classId, planStudentId);
        planStudents.forEach(planStudent -> {
            planStudent.setResultNoticeConfig(resultNoticeConfig);
            if (Objects.nonNull(resultNoticeConfig) && Objects.nonNull(resultNoticeConfig.getQrCodeFileId())) {
                planStudent.setNoticeQrCodeFileUrl(resourceFileService.getResourcePath(resultNoticeConfig.getQrCodeFileId()));
            }
        });
        return planStudents;
    }

//    public void asyncGeneratorPDF() {
//        String fileName;
//        html2PdfService.asyncGeneratorPDF(url, fileName, UUID.randomUUID().toString());
//    }
//
//    public PdfResponseDTO syncGeneratorPDF(Integer planId, Integer schoolId, Integer gradeId, Integer classId, Integer orgId,
//                                           Integer planStudentId, Boolean isSchoolClient) {
//        String fileName;
//        String screeningNoticeResultHtmlUrl = String.format(SCREENING_NOTICE_RESULT_HTML_URL, htmlUrlHost, planId, schoolId, gradeId, classId, );
//        return html2PdfService.syncGeneratorPDF(url, fileName, UUID.randomUUID().toString());
//    }
}
