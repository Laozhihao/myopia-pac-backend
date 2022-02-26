package com.wupol.myopia.business.aggregation.export.excel.imports;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.aggregation.export.excel.domain.UnbindScreeningStudentDTO;
import com.wupol.myopia.business.aggregation.export.utils.CommonCheck;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.common.utils.util.IdCardUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.parent.domain.model.ParentStudent;
import com.wupol.myopia.business.core.parent.service.ParentStudentService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningCodeGenerator;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 筛查学生
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class PlanStudentExcelImportService {

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private StudentService studentService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolStudentExcelImportService schoolStudentExcelImportService;

    @Resource
    private CommonImportService commonImportService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private ParentStudentService parentStudentService;

    @Resource
    private HospitalStudentService hospitalStudentService;

    @Resource
    private SchoolStudentService schoolStudentService;

    @Resource
    private SchoolClassService schoolClassService;

    /**
     * 导入筛查学生信息
     *
     * @param userId        学生信息
     * @param multipartFile 文件
     * @param schoolId      学校Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void importScreeningSchoolStudents(Integer userId, MultipartFile multipartFile, ScreeningPlan screeningPlan, Integer schoolId) {

        List<Map<Integer, String>> listMap = FileUtils.readExcel(multipartFile);
        if (CollectionUtils.isEmpty(listMap)) {
            // 无数据，直接返回
            return;
        }
        insertByUpload(userId, listMap, screeningPlan, schoolId);
        screeningPlanService.updateStudentNumbers(userId, screeningPlan.getId(), screeningPlanSchoolStudentService.getCountByScreeningPlanId(screeningPlan.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertByUpload(Integer userId, List<Map<Integer, String>> listMap, ScreeningPlan screeningPlan, Integer schoolId) {
        School school = schoolService.getById(schoolId);
        // 校验学校是否存在，表格中必填项是否都有
        checkRequiredInfo(school, listMap);
        // 获取所有身份证号、护照、筛查编码、学号
        List<String> idCardList = new ArrayList<>();
        List<String> passportList = new ArrayList<>();
        List<Long> screeningCodeList = new ArrayList<>();
        List<String> snoList = new ArrayList<>();
        generateBaseInfo(listMap, idCardList, passportList, snoList, screeningCodeList);
        // 获取计划下已经存在的筛查学生
        List<ScreeningPlanSchoolStudent> existPlanSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlan.getId());
        Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap = existPlanSchoolStudentList.stream().filter(s -> StringUtils.isNotBlank(s.getIdCard())).collect(Collectors.toMap(ScreeningPlanSchoolStudent::getIdCard, Function.identity()));
        Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap = existPlanSchoolStudentList.stream().filter(s -> StringUtils.isNotBlank(s.getPassport())).collect(Collectors.toMap(ScreeningPlanSchoolStudent::getPassport, Function.identity()));
        Map<Long, ScreeningPlanSchoolStudent> existPlanStudentScreeningCodeMap = existPlanSchoolStudentList.stream().filter(s -> Objects.nonNull(s.getScreeningCode())).collect(Collectors.toMap(ScreeningPlanSchoolStudent::getScreeningCode, Function.identity()));
        List<Long> existScreeningCode = existPlanSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getScreeningCode).filter(Objects::nonNull).collect(Collectors.toList());
        // 获取已经存在的多端学生
        List<Student> existManagementStudentList = studentService.getByIdCardsOrPassports(idCardList, passportList);
        TwoTuple<Map<String, Student>, Map<String, Student>> groupingMap = groupingByIdCardAndPassport(existManagementStudentList);
        Map<String, Student> existManagementStudentIdCardMap = groupingMap.getFirst();
        Map<String, Student> existManagementStudentPassportMap = groupingMap.getSecond();
        // 检查数据中是否有重复数据
        CommonCheck.checkHaveDuplicate(idCardList, snoList, passportList, false);
        // 获取班级信息
        Map<Integer, List<SchoolGradeExportDTO>> schoolGradeMaps = schoolGradeService.getGradeAndClassMap(Lists.newArrayList(school.getId()));
        List<Student> noScreeningCodeManagementStudentList = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> haveCredentialPlanStudent = new ArrayList<>();
        List<Student> haveCredentialStudent = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> noCredentialHaveStudentPlanStudents = new ArrayList<>();
        List<Student> noCredentialStudents = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> virtualStudentList = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> noCredentialPlanStudents = new ArrayList<>();
        List<UnbindScreeningStudentDTO> unbindList = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            String screeningCode = item.get(ImportExcelEnum.SCREENING_CODE.getIndex());
            String gradeName = item.get(ImportExcelEnum.GRADE.getIndex());
            String className = item.get(ImportExcelEnum.CLASS.getIndex());
            String idCard = item.get(ImportExcelEnum.ID_CARD.getIndex());
            String passport = item.get(ImportExcelEnum.PASSPORT.getIndex());
            String phone = item.get(ImportExcelEnum.PHONE.getIndex());
            String sno = item.get(ImportExcelEnum.STUDENT_NO.getIndex());
            Integer gender = StringUtils.isBlank(item.get(ImportExcelEnum.GENDER.getIndex())) ? IdCardUtil.getGender(item.get(ImportExcelEnum.ID_CARD.getIndex())) : GenderEnum.getType(item.get(ImportExcelEnum.GENDER.getIndex()));
            String studentName = item.get(ImportExcelEnum.NAME.getIndex());
            Integer nation = StringUtils.isBlank(item.get(ImportExcelEnum.NATION.getIndex())) ? null : NationEnum.getCode(item.get(ImportExcelEnum.NATION.getIndex()));
            Date birthday;
            if (StringUtils.isNotBlank(idCard) && !IdcardUtil.isValidCard(idCard)) {
                throw new BusinessException("上传失败" + idCard + "身份证异常");
            }
            if (StringUtils.isNotBlank(phone) && !PhoneUtil.isPhone(phone)) {
                throw new BusinessException("上传失败" + phone + "手机异常");
            }
            try {
                birthday = StringUtils.isBlank(item.get(ImportExcelEnum.BIRTHDAY.getIndex())) ? IdCardUtil.getBirthDay(item.get(ImportExcelEnum.ID_CARD.getIndex())) : DateFormatUtil.parseDate(item.get(ImportExcelEnum.BIRTHDAY.getIndex()), DateFormatUtil.FORMAT_ONLY_DATE2);
            } catch (ParseException e) {
                throw new BusinessException(getErrorMsgDate(idCard, passport, screeningCode) + "日期转换异常");
            }
            // 如果同时存在身份证和护照，优先取身份证
            if (StringUtils.isNoneBlank(idCard, passport)) {
                passport = null;
            }
            // 班级年级信息
            TwoTuple<Integer, Integer> gradeClassInfo = schoolStudentExcelImportService.getSchoolStudentClassInfo(schoolId, schoolGradeMaps, gradeName, className);
            Integer gradeType = GradeCodeEnum.getByName(gradeName).getType();
            // 检查学号
            checkSno(existPlanSchoolStudentList, sno, idCard, passport);
            // 是否带筛查编码一起上传
            if (StringUtils.isBlank(screeningCode)) {
                notScreeningCodeUpload(userId, existPlanStudentIdCardMap, existPlanStudentPassportMap, existManagementStudentIdCardMap, existManagementStudentPassportMap, noScreeningCodeManagementStudentList, idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, schoolId, phone);
                continue;
            }
            // 检查筛查编码是否存在
            if (!existScreeningCode.contains(Long.valueOf(screeningCode))) {
                throw new BusinessException("上传失败：筛查编码:" + screeningCode + "在计划中不存在");
            }
            // 是否带着证件号一起上传
            if (StringUtils.isAllBlank(idCard, passport)) {
                notCredentialUpload(existPlanStudentScreeningCodeMap, virtualStudentList, screeningCode, idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, phone, school, gradeType);
                continue;
            }
            // 筛查编码是否绑定身份证或护照
            ScreeningPlanSchoolStudent planSchoolStudent = existPlanStudentScreeningCodeMap.get(Long.valueOf(screeningCode));
            // 筛查编码没绑定证件号
            if (StringUtils.isAllBlank(planSchoolStudent.getIdCard(), planSchoolStudent.getPassport())) {
                ScreeningPlanSchoolStudent planStudent = getPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, idCard, passport);
                if (ObjectsUtil.allNotNull(planStudent, planSchoolStudent) && ObjectsUtil.allNotNull(planStudent.getScreeningCode(), planSchoolStudent.getScreeningCode()) && !planStudent.getScreeningCode().equals(planSchoolStudent.getScreeningCode())) {
                    throw new BusinessException("筛查编码" + screeningCode + "信息异常：身份证/护照重复");
                }
                notBindCredentialUpload(userId, existManagementStudentIdCardMap, existManagementStudentPassportMap, noCredentialHaveStudentPlanStudents, noCredentialStudents, noCredentialPlanStudents, idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, planSchoolStudent, phone, school);
                continue;
            }
            haveCredentialUpload(userId, existPlanStudentIdCardMap, existPlanStudentPassportMap, existManagementStudentIdCardMap, existManagementStudentPassportMap, haveCredentialPlanStudent, haveCredentialStudent, idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, planSchoolStudent, school, phone, unbindList);
        }
        updateOrSaveStudentInfo(screeningPlan, school, existPlanStudentIdCardMap, existPlanStudentPassportMap, noScreeningCodeManagementStudentList, haveCredentialPlanStudent, haveCredentialStudent, noCredentialHaveStudentPlanStudents, noCredentialStudents, virtualStudentList, noCredentialPlanStudents, unbindList, existManagementStudentIdCardMap, existManagementStudentPassportMap, userId);
    }

    /**
     * 存在证件信息上传
     */
    private void haveCredentialUpload(Integer userId, Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap,
                                      Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap,
                                      Map<String, Student> existManagementStudentIdCardMap,
                                      Map<String, Student> existManagementStudentPassportMap,
                                      List<ScreeningPlanSchoolStudent> haveCredentialPlanStudent,
                                      List<Student> haveCredentialStudent,
                                      String idCard, String passport, String sno, Integer gender, String studentName,
                                      Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, Integer gradeType,
                                      ScreeningPlanSchoolStudent planSchoolStudent, School school, String phone,
                                      List<UnbindScreeningStudentDTO> unbindList) {
        // 通过证件信息获取系统中已经存在的筛查学生
        ScreeningPlanSchoolStudent existPlanStudent = getPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, idCard, passport);
        // 如果上一步中的筛查学生与上传的筛查学生筛查编码对应不上，说明这个证件信息已经被使用了
        if (Objects.nonNull(existPlanStudent.getScreeningCode()) && !planSchoolStudent.getScreeningCode().equals(existPlanStudent.getScreeningCode())) {
            throw new BusinessException(getErrorMsgDate(idCard, passport, planSchoolStudent.getScreeningCode().toString()) + "已经存在系统中，请确认");
        }

        // 判断绑定的证件号是否一致
        UnbindScreeningStudentDTO unbindScreeningStudentDTO = checkCredentialInfo(idCard, passport, planSchoolStudent, sno, gender, studentName, nation, birthday, gradeClassInfo, school, phone, gradeType);
        if (Objects.nonNull(unbindScreeningStudentDTO)) {
            unbindList.add(unbindScreeningStudentDTO);
            return;
        }
        isSameCredential(userId, existPlanStudentIdCardMap, existPlanStudentPassportMap, existManagementStudentIdCardMap, existManagementStudentPassportMap, haveCredentialPlanStudent, haveCredentialStudent, idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, school, phone);
    }

    /**
     * 证件号一致
     */
    private void isSameCredential(Integer userId, Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap, Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap, Map<String, Student> existManagementStudentIdCardMap, Map<String, Student> existManagementStudentPassportMap, List<ScreeningPlanSchoolStudent> haveCredentialPlanStudent, List<Student> haveCredentialStudent, String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, Integer gradeType, School school, String phone) {
        // 更新学生和筛查学生
        TwoTuple<Student, ScreeningPlanSchoolStudent> twoTuple = getStudentAndPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport);
        Student student = twoTuple.getFirst();
        packageManagementStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, userId, school.getId(), student, phone);

        ScreeningPlanSchoolStudent planStudent = twoTuple.getSecond();
        packagePlanStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, planStudent, phone, school, gradeType);

        haveCredentialStudent.add(student);
        haveCredentialPlanStudent.add(planStudent);
    }

    /**
     * 筛查学生没绑定证件信息
     */
    private void notBindCredentialUpload(Integer userId, Map<String, Student> existManagementStudentIdCardMap, Map<String, Student> existManagementStudentPassportMap, List<ScreeningPlanSchoolStudent> noCredentialHaveStudentPlanStudents, List<Student> noCredentialStudents, List<ScreeningPlanSchoolStudent> noCredentialPlanStudents, String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, Integer gradeType, ScreeningPlanSchoolStudent planSchoolStudent, String phone, School school) {
        // 是否在系统中存在
        Student student = getStudent(existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport);
        packagePlanStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, planSchoolStudent, phone, school, gradeType);
        // 不存在
        if (Objects.isNull(student.getId())) {
            noCredentialPlanStudents.add(planSchoolStudent);
        }
        packageManagementStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, userId, school.getId(), student, phone);
        noCredentialStudents.add(student);
        // 已经存在，不更新多端学生
        planSchoolStudent.setStudentId(student.getId());
        noCredentialHaveStudentPlanStudents.add(planSchoolStudent);
    }

    /**
     * 没有证件信息的上传
     */
    private void notCredentialUpload(Map<Long, ScreeningPlanSchoolStudent> existPlanStudentScreeningCodeMap, List<ScreeningPlanSchoolStudent> virtualStudentList, String screeningCode, String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, String phone, School school, Integer gradeType) {
        ScreeningPlanSchoolStudent planSchoolStudent = existPlanStudentScreeningCodeMap.get(Long.valueOf(screeningCode));
        packagePlanStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, planSchoolStudent, phone, school, gradeType);
        virtualStudentList.add(planSchoolStudent);
    }

    /**
     * 没有筛查编码的上传
     */
    private void notScreeningCodeUpload(Integer userId, Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap, Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap, Map<String, Student> existManagementStudentIdCardMap, Map<String, Student> existManagementStudentPassportMap, List<Student> noScreeningCodeManagementStudentList, String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, Integer gradeType, Integer schoolId, String phone) {
        if (StringUtils.isAllBlank(idCard, passport)) {
            throw new BusinessException("上传失败：身份证、护照信息异常");
        }
        TwoTuple<Student, ScreeningPlanSchoolStudent> twoTuple = getStudentAndPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport);
        Student student = twoTuple.getFirst();
        packageManagementStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, userId, schoolId, student, phone);
        noScreeningCodeManagementStudentList.add(student);
    }

    /**
     * 判断绑定的证件号是否一致
     *
     * @param idCard            身份证
     * @param passport          护照
     * @param planSchoolStudent 筛查学生
     */
    private UnbindScreeningStudentDTO checkCredentialInfo(String idCard, String passport, ScreeningPlanSchoolStudent planSchoolStudent, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, School school, String phone, Integer gradeType) {
        String oldIdCard = planSchoolStudent.getIdCard();
        String oldPassport = planSchoolStudent.getPassport();
        if ((StringUtils.isNoneBlank(idCard, oldIdCard) && !StringUtils.equals(idCard, oldIdCard)) || StringUtils.isNoneBlank(passport, oldPassport) && !StringUtils.equals(passport, oldPassport)) {
            packagePlanStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, planSchoolStudent, phone, school, gradeType);
            return new UnbindScreeningStudentDTO(oldIdCard, oldPassport, planSchoolStudent);
        }
        if (StringUtils.isNoneBlank(idCard, oldPassport) || StringUtils.isNoneBlank(passport, oldIdCard)) {
            packagePlanStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, planSchoolStudent, phone, school, gradeType);
            return new UnbindScreeningStudentDTO(oldIdCard, oldPassport, planSchoolStudent);
        }
        return null;
    }

    /**
     * 更新获取插入学校学生、多端学生、筛查学生
     */
    private void updateOrSaveStudentInfo(ScreeningPlan screeningPlan, School school, Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap,
                                         Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap,
                                         List<Student> noScreeningCodeManagementStudentList,
                                         List<ScreeningPlanSchoolStudent> haveCredentialPlanStudent,
                                         List<Student> haveCredentialStudent,
                                         List<ScreeningPlanSchoolStudent> noCredentialHaveStudentPlanStudents,
                                         List<Student> noCredentialStudents,
                                         List<ScreeningPlanSchoolStudent> virtualStudentList,
                                         List<ScreeningPlanSchoolStudent> noCredentialPlanStudents,
                                         List<UnbindScreeningStudentDTO> unbindList,
                                         Map<String, Student> existManagementStudentIdCardMap,
                                         Map<String, Student> existManagementStudentPassportMap, Integer userId) {
        if (!CollectionUtils.isEmpty(noScreeningCodeManagementStudentList)) {
            saveStudentAndPlanStudent(noScreeningCodeManagementStudentList, existPlanStudentIdCardMap, existPlanStudentPassportMap, screeningPlan, school);
        }
        if (!CollectionUtils.isEmpty(noCredentialStudents)) {
            updateOrSaveNoCredentialStudent(noCredentialStudents, noCredentialPlanStudents, screeningPlan);
        }
        if (!CollectionUtils.isEmpty(noCredentialHaveStudentPlanStudents)) {
            updatePlanStudentAndVisionResult(screeningPlan, noCredentialHaveStudentPlanStudents);
        }
        if (!CollectionUtils.isEmpty(virtualStudentList)) {
            updatePlanStudentAndVisionResult(screeningPlan, virtualStudentList);
        }
        if (!CollectionUtils.isEmpty(haveCredentialPlanStudent)) {
            updatePlanStudentAndVisionResult(screeningPlan, haveCredentialPlanStudent);
        }
        if (!CollectionUtils.isEmpty(haveCredentialStudent)) {
            studentService.saveOrUpdateBatch(haveCredentialStudent);
            // 插入学校端
            commonImportService.insertSchoolStudent(haveCredentialStudent);
        }
        if (!CollectionUtils.isEmpty(unbindList)) {
            unbindStudent(unbindList, screeningPlan, existManagementStudentIdCardMap, existManagementStudentPassportMap, userId);
        }
    }

    /**
     * 更新没有证件的学生
     */
    private void updateOrSaveNoCredentialStudent(List<Student> noCredentialStudents, List<ScreeningPlanSchoolStudent> noCredentialPlanStudents, ScreeningPlan screeningPlan) {
        studentService.saveOrUpdateBatch(noCredentialStudents);
        // 插入学校端
        commonImportService.insertSchoolStudent(noCredentialStudents);
        TwoTuple<Map<String, Student>, Map<String, Student>> groupingStudentMap = groupingByIdCardAndPassport(noCredentialStudents);
        Map<String, Student> idCardMap = groupingStudentMap.getFirst();
        Map<String, Student> passportMap = groupingStudentMap.getSecond();
        for (ScreeningPlanSchoolStudent planSchoolStudent : noCredentialPlanStudents) {
            Student student = getStudent(idCardMap, passportMap, planSchoolStudent.getIdCard(), planSchoolStudent.getPassport());
            if (Objects.isNull(student.getId())) {
                throw new BusinessException("学生数据异常");
            }
            planSchoolStudent.setStudentId(student.getId());
        }
        updatePlanStudentAndVisionResult(screeningPlan, noCredentialPlanStudents);
    }

    /**
     * 列表通过身份证和护照分组
     *
     * @param managementStudentList 管理端学生
     * @return first-身份证 second-护照
     */
    private TwoTuple<Map<String, Student>, Map<String, Student>> groupingByIdCardAndPassport(List<Student> managementStudentList) {
        Map<String, Student> idCardMap = managementStudentList.stream().filter(s -> StringUtils.isNotBlank(s.getIdCard())).collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        Map<String, Student> passportMap = managementStudentList.stream().filter(s -> StringUtils.isNotBlank(s.getPassport())).collect(Collectors.toMap(Student::getPassport, Function.identity()));
        return new TwoTuple<>(idCardMap, passportMap);
    }

    /**
     * 更新或新增计划学生和多端学生
     *
     * @param managementStudentList       多端学生
     * @param existPlanStudentIdCardMap   身份证
     * @param existPlanStudentPassportMap 护照信息
     * @param plan                        计划
     * @param school                      学校
     */
    private void saveStudentAndPlanStudent(List<Student> managementStudentList, Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap, Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap, ScreeningPlan plan, School school) {

        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(managementStudentList.stream().map(Student::getGradeId).collect(Collectors.toList()));
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(managementStudentList.stream().map(Student::getClassId).collect(Collectors.toList()));

        List<ScreeningPlanSchoolStudent> list = new ArrayList<>();
        studentService.saveOrUpdateBatch(managementStudentList);
        managementStudentList.forEach(student -> {
            ScreeningPlanSchoolStudent planStudent = getPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, student.getIdCard(), student.getPassport());
            planStudent.setScreeningPlanId(plan.getId());
            planStudent.setStudentId(student.getId());
            planStudent.setSrcScreeningNoticeId(plan.getSrcScreeningNoticeId());
            planStudent.setScreeningTaskId(plan.getScreeningTaskId());
            planStudent.setStudentAge(DateUtil.ageOfNow(student.getBirthday()));
            planStudent.setScreeningOrgId(plan.getScreeningOrgId());
            planStudent.setSchoolName(school.getName());
            planStudent.setSchoolId(school.getId());
            planStudent.setSchoolDistrictId(school.getDistrictId());
            planStudent.setSchoolDistrictId(school.getDistrictId());
            planStudent.setGradeType(student.getGradeType());
            planStudent.setSchoolDistrictId(school.getDistrictId());
            planStudent.setPlanDistrictId(plan.getDistrictId());
            if (Objects.isNull(planStudent.getScreeningCode())) {
                planStudent.setScreeningCode(ScreeningCodeGenerator.nextId());
            }
            planStudent.setGradeName(Objects.nonNull(student.getGradeId()) ? gradeMap.get(student.getGradeId()).getName() : null);
            planStudent.setClassName(Objects.nonNull(student.getClassId()) ? classMap.get(student.getClassId()).getName() : null);
            packagePlanStudentByStudent(student, planStudent);
            list.add(planStudent);
        });
        // 插入学校端
        commonImportService.insertSchoolStudent(managementStudentList);
        updatePlanStudentAndVisionResult(plan, list);
    }

    /**
     * 设置多端学生
     */
    private void packageManagementStudent(String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, Integer gradeType, Integer userId, Integer schoolId, Student student, String phone) {
        student.setIdCard(idCard);
        student.setPassport(passport);
        student.setName(studentName);
        student.setBirthday(birthday);
        student.setGender(gender);
        student.setNation(nation);
        student.setGradeId(gradeClassInfo.getFirst());
        student.setClassId(gradeClassInfo.getSecond());
        student.setSno(sno);
        student.setGradeType(gradeType);
        student.setCreateUserId(userId);
        student.setSchoolId(schoolId);
        student.setParentPhone(phone);
    }

    /**
     * 设置计划学生
     */
    private void packagePlanStudent(String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, ScreeningPlanSchoolStudent planStudent, String phone, School school, Integer gradeType) {
        planStudent.setIdCard(idCard);
        planStudent.setPassport(passport);
        planStudent.setStudentName(studentName);
        planStudent.setBirthday(birthday);
        planStudent.setGender(gender);
        planStudent.setNation(nation);
        planStudent.setGradeId(gradeClassInfo.getFirst());
        planStudent.setClassId(gradeClassInfo.getSecond());
        planStudent.setStudentNo(sno);
        planStudent.setParentPhone(phone);
        planStudent.setSchoolId(school.getId());
        planStudent.setSchoolName(school.getName());
        planStudent.setSchoolDistrictId(school.getDistrictId());
        planStudent.setGradeType(gradeType);
    }

    /**
     * 设置计划学生
     *
     * @param student     学生
     * @param planStudent 计划学生
     */
    private void packagePlanStudentByStudent(Student student, ScreeningPlanSchoolStudent planStudent) {
        planStudent.setIdCard(student.getIdCard());
        planStudent.setPassport(student.getPassport());
        planStudent.setStudentName(student.getName());
        planStudent.setBirthday(student.getBirthday());
        planStudent.setGender(student.getGender());
        planStudent.setNation(student.getNation());
        planStudent.setGradeId(student.getGradeId());
        planStudent.setClassId(student.getClassId());
        planStudent.setStudentNo(student.getSno());
        planStudent.setStudentId(student.getId());
        planStudent.setParentPhone(student.getParentPhone());
    }


    /**
     * 生成学生基本信息
     *
     * @param listMap           上传列表
     * @param idCardList        身份证
     * @param passportList      护照
     * @param snoList           学号
     * @param screeningCodeList 筛查编号
     */
    private void generateBaseInfo(List<Map<Integer, String>> listMap, List<String> idCardList, List<String> passportList, List<String> snoList, List<Long> screeningCodeList) {
        listMap.forEach(item -> {
            String idCard = item.getOrDefault(ImportExcelEnum.ID_CARD.getIndex(), null);
            if (StringUtils.isNotBlank(idCard)) {
                idCardList.add(idCard);
            }

            String passport = item.getOrDefault(ImportExcelEnum.PASSPORT.getIndex(), null);
            if (StringUtils.isNotBlank(passport)) {
                if (passport.length() < 7) {
                    throw new BusinessException("护照" + passport + "格式异常");
                }
                passportList.add(passport);
            }

            String sno = item.getOrDefault(ImportExcelEnum.STUDENT_NO.getIndex(), null);
            if (StringUtils.isNotBlank(sno)) {
                snoList.add(sno);
            }

            String screeningCode = item.getOrDefault(ImportExcelEnum.SCREENING_CODE.getIndex(), null);
            if (StringUtils.isNotBlank(screeningCode)) {
                screeningCodeList.add(Long.valueOf(screeningCode));
            }
        });
    }

    /**
     * 校验学校是否存在，表格中必填项是否都有
     *
     * @param school  学校
     * @param listMap 上传列表
     */
    private void checkRequiredInfo(School school, List<Map<Integer, String>> listMap) {
        if (Objects.isNull(school)) {
            throw new BusinessException("不存在该学校");
        }
        // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
        listMap.forEach(item -> {
            if (ObjectsUtil.hasNull(item.getOrDefault(ImportExcelEnum.NAME.getIndex(), null), item.getOrDefault(ImportExcelEnum.GRADE.getIndex(), null), item.getOrDefault(ImportExcelEnum.CLASS.getIndex(), null))) {
                throw new BusinessException("存在必填项无填写");
            }
            if (Objects.isNull(item.getOrDefault(ImportExcelEnum.ID_CARD.getIndex(), null)) && Objects.isNull(item.getOrDefault(ImportExcelEnum.GENDER.getIndex(), null))) {
                throw new BusinessException("性别不能为空");
            }
        });
    }

    /**
     * 获取学生
     *
     * @param existPlanStudentIdCardMap         身份证Map
     * @param existPlanStudentPassportMap       护照Map
     * @param existManagementStudentIdCardMap   身份证Map
     * @param existManagementStudentPassportMap 护照Map
     * @param idCard                            身份证
     * @param passport                          护照
     * @return first-多端学生 second-筛查学生
     */
    private TwoTuple<Student, ScreeningPlanSchoolStudent> getStudentAndPlanStudent(Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap, Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap, Map<String, Student> existManagementStudentIdCardMap, Map<String, Student> existManagementStudentPassportMap, String idCard, String passport) {

        return new TwoTuple<>(getStudent(existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport), getPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, idCard, passport));
    }

    /**
     * 获取学生
     *
     * @param existManagementStudentIdCardMap   身份证Map
     * @param existManagementStudentPassportMap 护照Map
     * @param idCard                            身份证
     * @param passport                          护照
     * @return 学生
     */
    private Student getStudent(Map<String, Student> existManagementStudentIdCardMap, Map<String, Student> existManagementStudentPassportMap, String idCard, String passport) {
        Student student;
        if (Objects.nonNull(idCard)) {
            student = existManagementStudentIdCardMap.get(idCard);
        } else {
            student = existManagementStudentPassportMap.get(passport);
        }
        if (Objects.isNull(student)) {
            student = new Student();
        }
        return student;
    }

    /**
     * 获取筛查学生
     *
     * @param existPlanStudentIdCardMap   身份证Map
     * @param existPlanStudentPassportMap 护照Map
     * @param idCard                      身份证
     * @param passport                    护照
     * @return 筛查学生
     */
    private ScreeningPlanSchoolStudent getPlanStudent(Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap, Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap, String idCard, String passport) {
        ScreeningPlanSchoolStudent planSchoolStudent;
        if (Objects.nonNull(idCard)) {
            planSchoolStudent = existPlanStudentIdCardMap.get(idCard);
        } else {
            planSchoolStudent = existPlanStudentPassportMap.get(passport);
        }
        if (Objects.isNull(planSchoolStudent)) {
            planSchoolStudent = new ScreeningPlanSchoolStudent();
        }
        return planSchoolStudent;
    }

    /**
     * 获取有值的数据
     *
     * @param idCard        身份证
     * @param passport      护照
     * @param screeningCode 编码
     * @return 数据
     */
    private String getErrorMsgDate(String idCard, String passport, String screeningCode) {
        return "身份证/护照/筛查编码：" + (StringUtils.isNotBlank(idCard) ? idCard : StringUtils.isNotBlank(passport) ? passport : screeningCode);
    }

    /**
     * 校验学号是否重复
     *
     * @param existPlanSchoolStudentList 已经存在的学生
     * @param sno                        学号
     * @param idCard                     身份证
     * @param passport                   护照
     */
    private void checkSno(List<ScreeningPlanSchoolStudent> existPlanSchoolStudentList, String sno, String idCard, String passport) {
        if (CollectionUtils.isEmpty(existPlanSchoolStudentList) || StringUtils.isBlank(sno)) {
            return;
        }
        for (ScreeningPlanSchoolStudent s : existPlanSchoolStudentList) {
            // 学号是否被使用
            if (StringUtils.equals(sno, s.getStudentNo()) && ((StringUtils.isNotBlank(idCard) && !StringUtils.equals(idCard, s.getIdCard())) || (StringUtils.isNotBlank(passport) && !StringUtils.equals(passport, s.getPassport())))) {
                throw new BusinessException("学号:" + sno + "重复");
            }
        }
    }

    /**
     * 更新学生计划结果
     *
     * @param plan         计划
     * @param planStudents 筛查学生
     */
    private void updatePlanStudentAndVisionResult(ScreeningPlan plan, List<ScreeningPlanSchoolStudent> planStudents) {
        if (CollectionUtils.isEmpty(planStudents)) {
            return;
        }
        screeningPlanSchoolStudentService.saveOrUpdateBatch(planStudents);
        // 获取所有结果
        List<VisionScreeningResult> resultList = visionScreeningResultService.getByPlanId(plan.getId());
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }

        List<VisionScreeningResult> updateResultList = new ArrayList<>();

        Map<Integer, VisionScreeningResult> visionMap = resultList.stream().collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));
        planStudents.forEach(planStudent -> {
            VisionScreeningResult result = visionMap.get(planStudent.getId());
            if (Objects.nonNull(result)) {
                result.setStudentId(planStudent.getStudentId());
                result.setSchoolId(planStudent.getSchoolId());
                updateResultList.add(result);
            }
        });
        visionScreeningResultService.updateBatchById(updateResultList);
    }

    /**
     * 解除绑定学生
     */
    private void unbindStudent(List<UnbindScreeningStudentDTO> unbindList, ScreeningPlan screeningPlan, Map<String, Student> existManagementStudentIdCardMap, Map<String, Student> existManagementStudentPassportMap, Integer userId) {
        List<Integer> studentIds = studentService.getByIdCardsOrPassports(unbindList.stream().map(UnbindScreeningStudentDTO::getIdCard).collect(Collectors.toList()), unbindList.stream().map(UnbindScreeningStudentDTO::getPassport).collect(Collectors.toList())).stream().map(Student::getId).collect(Collectors.toList());
        List<Integer> deletedStudent = new ArrayList<>();
        Map<Integer, VisionScreeningResult> resultMap = visionScreeningResultService.getByStudentIds(studentIds).stream().collect(Collectors.toMap(VisionScreeningResult::getStudentId, Function.identity(), (s1, s2) -> s1));
        Map<Integer, ParentStudent> parentStudentMap = parentStudentService.getByStudentIds(studentIds).stream().collect(Collectors.toMap(ParentStudent::getStudentId, Function.identity(), (s1, s2) -> s1));
        Map<Integer, HospitalStudent> hospitalStudentMap = hospitalStudentService.getByStudentIds(studentIds).stream().collect(Collectors.toMap(HospitalStudent::getStudentId, Function.identity(), (s1, s2) -> s1));

        List<ScreeningPlanSchoolStudent> noDateBindPlanStudent = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> haveDatePlanStudent = new ArrayList<>();
        unbindList.forEach(s -> {
            Integer studentId = s.getScreeningPlanSchoolStudent().getStudentId();
            if (ObjectsUtil.allNull(resultMap.get(studentId), parentStudentMap.get(studentId), hospitalStudentMap.get(studentId))) {
                deletedStudent.add(studentId);
                noDateBindPlanStudent.add(s.getScreeningPlanSchoolStudent());
            } else {
                haveDatePlanStudent.add(s.getScreeningPlanSchoolStudent());
            }
        });
        deletedUnbindStudent(noDateBindPlanStudent, deletedStudent, screeningPlan, userId,existManagementStudentIdCardMap ,existManagementStudentPassportMap);
        havaDateUnbindStudent(haveDatePlanStudent, existManagementStudentIdCardMap, existManagementStudentPassportMap, screeningPlan, userId);
    }

    /**
     * 其他端没有数据，进行删除
     */
    private void deletedUnbindStudent(List<ScreeningPlanSchoolStudent> noDateBindPlanStudent, List<Integer> deletedStudent,
                                      ScreeningPlan screeningPlan, Integer userId, Map<String, Student> existManagementStudentIdCardMap,
                                      Map<String, Student> existManagementStudentPassportMap) {
        if (CollectionUtils.isEmpty(noDateBindPlanStudent)) {
            return;
        }
        log.error("删除学生逻辑");
        screeningPlanSchoolStudentService.deleteByStudentIds(deletedStudent);
        studentService.removeByIds(deletedStudent);
        schoolStudentService.deleteByStudentIds(deletedStudent);

        List<Student> studentList = new ArrayList<>();
        noDateBindPlanStudent.forEach(s -> {
            Student student = getStudent(existManagementStudentIdCardMap, existManagementStudentPassportMap, s.getIdCard(), s.getPassport());
            Integer studentId = student.getId();
            student.setName(s.getStudentName());
            student.setSno(s.getStudentNo());
            student.setCreateUserId(userId);
            BeanUtils.copyProperties(s, student);
            student.setId(studentId);
            studentList.add(student);
        });
        studentService.saveOrUpdateBatch(studentList);
        unbindStudentSaveOrUpdate(noDateBindPlanStudent, studentList);
        updatePlanStudentAndVisionResult(screeningPlan, noDateBindPlanStudent);
    }

    /**
     * 其他端存在数据，则解除绑定
     */
    private void havaDateUnbindStudent(List<ScreeningPlanSchoolStudent> haveDatePlanStudent, Map<String, Student> existManagementStudentIdCardMap, Map<String, Student> existManagementStudentPassportMap, ScreeningPlan screeningPlan, Integer userId) {
        if (CollectionUtils.isEmpty(haveDatePlanStudent)) {
            return;
        }
        log.error("解除绑定学生逻辑");
        List<Student> studentList = new ArrayList<>();
        haveDatePlanStudent.forEach(s -> {
            Student student = getStudent(existManagementStudentIdCardMap, existManagementStudentPassportMap, s.getIdCard(), s.getPassport());
            Integer studentId = student.getId();
            BeanUtils.copyProperties(s, student);
            student.setId(studentId);
            student.setName(s.getStudentName());
            student.setSno(s.getStudentNo());
            student.setCreateUserId(userId);
            studentList.add(student);
        });
        studentService.saveOrUpdateBatch(studentList);
        unbindStudentSaveOrUpdate(haveDatePlanStudent, studentList);
        updatePlanStudentAndVisionResult(screeningPlan, haveDatePlanStudent);
    }

    /**
     * 计划学生设置学生Id
     */
    private void unbindStudentSaveOrUpdate(List<ScreeningPlanSchoolStudent> noDateBindPlanStudent, List<Student> studentList) {
        Map<String, Integer> studentIdCardMap = studentList.stream().filter(s -> StringUtils.isNotBlank(s.getIdCard())).collect(Collectors.toMap(Student::getIdCard, Student::getId));
        Map<String, Integer> studentPassportMap = studentList.stream().filter(s -> StringUtils.isNotBlank(s.getPassport())).collect(Collectors.toMap(Student::getPassport, Student::getId));
        noDateBindPlanStudent.forEach(planStudent -> {
            if (StringUtils.isNotBlank(planStudent.getIdCard())) {
                planStudent.setStudentId(studentIdCardMap.get(planStudent.getIdCard()));
            }
            if (StringUtils.isNotBlank(planStudent.getPassport())) {
                planStudent.setStudentId(studentPassportMap.get(planStudent.getPassport()));
            }
            planStudent.checkStudentInfo();
        });
    }

    public void checkAbc() {

    }
}
