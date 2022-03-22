package com.wupol.myopia.business.aggregation.screening.handler;

import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.screening.domain.dto.CredentialType;
import com.wupol.myopia.business.aggregation.screening.domain.dto.CredentialTypeAndContent;
import com.wupol.myopia.business.aggregation.screening.domain.dto.UpdatePlanStudentRequestDTO;
import com.wupol.myopia.business.aggregation.screening.service.CommonImportServiceCopy;
import com.wupol.myopia.business.common.utils.constant.SourceClientEnum;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.parent.domain.model.ParentStudent;
import com.wupol.myopia.business.core.parent.service.ParentStudentService;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Classname CredentialMotificationDTO
 * @Description 证件修改的情况, 为了处理哪些证件需要更新, 哪些证件需要丢弃
 * @Date 2022/2/25 1:33 下午
 * @Author Jacob
 * @Version
 */
@Getter
@Setter
@Service
@Slf4j
public class CredentialModificationHandler {
    @Resource
    private StudentService studentService;
    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private CommonImportServiceCopy commonImportServiceCopy;
    @Resource
    private VisionScreeningResultService visionScreeningResultService;
    @Resource
    private ParentStudentService parentStudentService;
    @Resource
    private HospitalStudentService hospitalStudentService;
    @Resource
    private SchoolStudentService schoolStudentService;
    @Resource
    private ScreeningPlanService screeningPlanService;
    @Resource
    private SchoolClassService schoolClassService;
    @Resource
    private SchoolGradeService schoolGradeService;

    /**
     * 获取处理结果
     *
     * @param oldCredential
     * @param newCredential
     * @return
     */
    private ProcessResult getProcessResult(CredentialTypeAndContent oldCredential, CredentialTypeAndContent newCredential) {
        //证件号不需要更新
        CredentialTypeAndContent updateCredential = null;
        CredentialTypeAndContent discardCredential = null;
        if (oldCredential == null) {
            updateCredential = newCredential;
        } else if (newCredential == null) {
            updateCredential = oldCredential;
        } else {
            //oldCredential != null && newCredential != null 的情况
            CredentialType oldCredentialType = oldCredential.getCredentialType();
            String oldCredentialContent = oldCredential.getCredentialContent();
            CredentialType newCredentialType = newCredential.getCredentialType();
            String newCredentialContent = newCredential.getCredentialContent();
            boolean sameCredentialContent = StringUtils.equals(oldCredentialContent, newCredentialContent);
            boolean sameCredentialType = oldCredentialType == newCredentialType;
            //证件类型一样,证件内容不一样的情况
            if (sameCredentialType && !sameCredentialContent) {
                //更新
                updateCredential = newCredential;
                //丢弃
                discardCredential = oldCredential;
            }

            //类型不一样的话,无论证件号是否一样
            if (!sameCredentialType) {
                //更新
                updateCredential = newCredential;
                //丢弃  //新旧都有,只要新的证件号不为空(到这里就不为空), 直接覆盖新的证件号, 以及看是否丢弃旧的
                discardCredential = oldCredential;
            }

            if (sameCredentialType && sameCredentialContent) {
                //什么都一样,正常更新就行
                updateCredential = newCredential;
            }
        }
        return new ProcessResult(updateCredential, discardCredential);
    }

    /**
     * 因为证件号的关系更新student
     *
     * @param updatePlanStudentRequestDTO
     * @param screeningPlanSchoolStudent
     */
    public void updateStudentByCredentialNO(UpdatePlanStudentRequestDTO updatePlanStudentRequestDTO, ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        if (screeningPlanSchoolStudent == null) {
            throw new BusinessException("业务异常,screeningPlanSchoolStudent 不能为空");
        }

        if (StringUtils.isNotBlank(screeningPlanSchoolStudent.getIdCard()) && StringUtils.isNotBlank(screeningPlanSchoolStudent.getPassport())) {
            throw new BusinessException("业务异常: id 和 passport同时存在, screeningPlanSchoolStudentId = " + screeningPlanSchoolStudent.getId());
        }
        CredentialModificationHandler.ProcessResult processResult = getResult(screeningPlanSchoolStudent.getIdCard(), screeningPlanSchoolStudent.getPassport(), updatePlanStudentRequestDTO.getIdCard(), updatePlanStudentRequestDTO.getPassport());
        //从多端学生中查找数据
        Student existStudentByCredentialNO = getExistStudentByCredentialNO(processResult.getUpdateCredential());
        //更新或者插入多端学生
        Student updateStudent = updateOrInsertByCredentialNO(processResult.getUpdateCredential(), existStudentByCredentialNO, updatePlanStudentRequestDTO);
        if (updateStudent != null) {
            screeningPlanSchoolStudent.setIdCard(updateStudent.getIdCard());
            screeningPlanSchoolStudent.setPassport(updateStudent.getPassport());
            screeningPlanSchoolStudent.setStudentId(updateStudent.getId());
        }
        screeningPlanSchoolStudent.setClassName(schoolClassService.getById(updatePlanStudentRequestDTO.getClassId()).getName());
        screeningPlanSchoolStudent.setGradeName(schoolGradeService.getById(updatePlanStudentRequestDTO.getGradeId()).getName());
        screeningPlanSchoolStudentService.updateById(screeningPlanSchoolStudent);
        // 更新筛查结果
        visionScreeningResultService.updatePlanStudentAndVisionResult(screeningPlanService.getById(screeningPlanSchoolStudent.getScreeningPlanId()), Lists.newArrayList(screeningPlanSchoolStudent));
        discardStudent(processResult.getDiscardCredential(), screeningPlanSchoolStudent.getScreeningPlanId());
    }


    /**
     * 初始化
     *
     * @param oldPassport
     * @param oldIdCard
     * @param newPassport
     * @param newIdCard
     * @return
     */
    private ProcessResult getResult(String oldIdCard, String oldPassport, String newIdCard, String newPassport) {
        CredentialTypeAndContent oldCredential = CredentialTypeAndContent.getInstance(oldIdCard, oldPassport);
        CredentialTypeAndContent newCredential = CredentialTypeAndContent.getInstance(newIdCard, newPassport);
        return getProcessResult(oldCredential, newCredential);
    }

    /**
     * 删除废弃的学生
     *
     * @param credentialTypeAndContent
     */
    private void discardStudent(CredentialTypeAndContent credentialTypeAndContent, Integer screeningPlanId) {
        if (credentialTypeAndContent == null) {
            log.info("根据证件号更新计划学生后, 没有旧的证件号的学生需要删除.");
            return;
        }
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getByIdCardAndPassport(credentialTypeAndContent.getIdCard(), credentialTypeAndContent.getPassport(), null);
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudents) && Objects.nonNull(credentialTypeAndContent.getCredentialType())) {
            Student student = studentService.getByIdCardAndPassport(credentialTypeAndContent.getIdCard(), credentialTypeAndContent.getPassport(), null);
            if (Objects.nonNull(student)) {
                deletedStudent(student.getId(), student.getSchoolId(), screeningPlanId);
            }
        }
    }


    /**
     * 更新或者插入一个student
     *
     * @param credentialTypeAndContent
     * @param existStudentByCredentialNO
     * @param updatePlanStudentRequestDTO
     * @return
     */
    private Student updateOrInsertByCredentialNO(CredentialTypeAndContent credentialTypeAndContent, Student existStudentByCredentialNO, UpdatePlanStudentRequestDTO updatePlanStudentRequestDTO) {
        if (existStudentByCredentialNO == null) {
            //新增数据
            Student newStudent = createNewStudent(updatePlanStudentRequestDTO);
            commonImportServiceCopy.insertSchoolStudent(Arrays.asList(newStudent));
            return newStudent;
        }
        return updateStudent(credentialTypeAndContent, existStudentByCredentialNO, updatePlanStudentRequestDTO);
    }

    /**
     * 更新学生
     *
     * @param credentialTypeAndContent
     * @param student
     * @param updatePlanStudentRequestDTO
     * @return
     */
    private Student updateStudent(CredentialTypeAndContent credentialTypeAndContent, Student student, UpdatePlanStudentRequestDTO updatePlanStudentRequestDTO) {
        //更新数据
        student.setName(updatePlanStudentRequestDTO.getName());
        student.setGender(updatePlanStudentRequestDTO.getGender());
        student.setBirthday(updatePlanStudentRequestDTO.getBirthday());
        student.setPassport(credentialTypeAndContent.getPassport());
        student.setIdCard(credentialTypeAndContent.getIdCard());
        student.setSchoolId(updatePlanStudentRequestDTO.getSchoolId());
        student.setClassId(updatePlanStudentRequestDTO.getClassId());
        student.setGradeId(updatePlanStudentRequestDTO.getGradeId());
        if (StringUtils.isNotBlank(updatePlanStudentRequestDTO.getParentPhone())) {
            student.setParentPhone(updatePlanStudentRequestDTO.getParentPhone());
        }
        if (StringUtils.isNotBlank(updatePlanStudentRequestDTO.getSno())) {
            student.setSno(updatePlanStudentRequestDTO.getSno());
        }
        studentService.updateStudent(student);
        return student;
    }

    /**
     * 创建参数
     *
     * @param updatePlanStudentRequestDTO
     */
    private Student createNewStudent(UpdatePlanStudentRequestDTO updatePlanStudentRequestDTO) {
        Student student = new Student();
        student.setIdCard(updatePlanStudentRequestDTO.getIdCard());
        student.setPassport(updatePlanStudentRequestDTO.getPassport());
        student.setName(updatePlanStudentRequestDTO.getName());
        student.setBirthday(updatePlanStudentRequestDTO.getBirthday());
        student.setGender(updatePlanStudentRequestDTO.getGender());
        student.setGradeId(updatePlanStudentRequestDTO.getGradeId());
        student.setClassId(updatePlanStudentRequestDTO.getClassId());
        student.setSno(updatePlanStudentRequestDTO.getSno());
        student.setCreateUserId(updatePlanStudentRequestDTO.getUserId());
        student.setSchoolId(updatePlanStudentRequestDTO.getSchoolId());
        student.setParentPhone(updatePlanStudentRequestDTO.getParentPhone());
        student.setSchoolId(updatePlanStudentRequestDTO.getSchoolId());
        student.setUpdateTime(new Date());
        student.setSourceClient(SourceClientEnum.SCREENING_PLAN.type);
        studentService.saveStudent(student);
        return student;
    }

    /**
     * @param credentialTypeAndContent
     * @return
     */
    public Student getExistStudentByCredentialNO(CredentialTypeAndContent credentialTypeAndContent) {
        if (credentialTypeAndContent.getCredentialType() == CredentialType.ID_CARD) {
            return studentService.getByIdCardAndPassport(credentialTypeAndContent.getCredentialContent(), null, null);
        }
        if (credentialTypeAndContent.getCredentialType() == CredentialType.PASSPORT) {
            return studentService.getByIdCardAndPassport(null, credentialTypeAndContent.getCredentialContent(), null);
        } else {
            throw new BusinessException("证件类型异常,type = " + credentialTypeAndContent.getCredentialType());
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProcessResult {
        private CredentialTypeAndContent updateCredential;
        private CredentialTypeAndContent discardCredential;
    }

    /**
     * 删除多端学生
     *
     * @param studentId 学生Id
     * @param planId    计划Id
     */
    public void deletedStudent(Integer studentId, Integer schoolId, Integer planId) {
        List<Integer> studentIds = Lists.newArrayList(studentId);
        Map<Integer, ScreeningPlanSchoolStudent> planStudentMap = screeningPlanSchoolStudentService.getByNePlanId(planId).stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getStudentId, Function.identity(), (s1, s2) -> s1));
        Map<Integer, SchoolStudent> schoolStudentMap = schoolStudentService.getByStudentIdsAndSchoolId(studentIds, schoolId).stream().collect(Collectors.toMap(SchoolStudent::getStudentId, Function.identity(), (s1, s2) -> s1));
        Map<Integer, VisionScreeningResult> resultMap = visionScreeningResultService.getByStudentIds(studentIds).stream().collect(Collectors.toMap(VisionScreeningResult::getStudentId, Function.identity(), (s1, s2) -> s1));
        Map<Integer, ParentStudent> parentStudentMap = parentStudentService.getByStudentIds(studentIds).stream().collect(Collectors.toMap(ParentStudent::getStudentId, Function.identity(), (s1, s2) -> s1));
        Map<Integer, HospitalStudent> hospitalStudentMap = hospitalStudentService.getByStudentIds(studentIds).stream().collect(Collectors.toMap(HospitalStudent::getStudentId, Function.identity(), (s1, s2) -> s1));
        if (ObjectsUtil.allNull(resultMap.get(studentId), parentStudentMap.get(studentId), hospitalStudentMap.get(studentId), planStudentMap.get(studentId))
                && schoolStudentService.isCanDeletedSchoolStudent(schoolStudentMap, studentId)
                && studentService.isCanDeletedStudent(studentId)) {
            screeningPlanSchoolStudentService.deleteByStudentIds(studentIds);
            studentService.removeByIds(studentIds);
            schoolStudentService.deleteByStudentIds(studentIds);
        }
    }
}
