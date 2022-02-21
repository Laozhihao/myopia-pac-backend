package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdcardUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.aggregation.export.excel.imports.SchoolStudentExcelImportService;
import com.wupol.myopia.business.aggregation.export.utils.CommonCheck;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.util.IdCardUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/26
 **/
@Log4j2
@Service
public class ExcelStudentService {

    @Autowired
    private StudentService studentService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private SchoolStudentExcelImportService schoolStudentExcelImportService;

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
        CommonCheck.checkHaveDuplicate(idCardList, snoList, passportList);
        // 获取班级信息
        Map<Integer, List<SchoolGradeExportDTO>> schoolGradeMaps = schoolGradeService.getGradeAndClassMap(Lists.newArrayList(school.getId()));
        List<Student> noScreeningCodeManagementStudentList = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> havePaperworkPlanStudent = new ArrayList<>();
        List<Student> havePaperworkStudent = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> noPaperworkHaveStudentPlanStudents = new ArrayList<>();
        List<Student> noPaperworkStudents = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> virtualStudentList = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> noPaperworkPlanStudents = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            String screeningCode = item.get(ImportExcelEnum.SCREENING_CODE.getIndex());
            String gradeName = item.get(ImportExcelEnum.GRADE.getIndex());
            String className = item.get(ImportExcelEnum.CLASS.getIndex());
            String idCard = item.get(ImportExcelEnum.ID_CARD.getIndex());
            String passport = item.get(ImportExcelEnum.PASSPORT.getIndex());
            String sno = item.get(ImportExcelEnum.STUDENT_NO.getIndex());
            Integer gender = StringUtils.isBlank(item.get(ImportExcelEnum.GENDER.getIndex())) ? IdCardUtil.getGender(item.get(ImportExcelEnum.ID_CARD.getIndex())) : GenderEnum.getType(item.get(ImportExcelEnum.GENDER.getIndex()));
            String studentName = item.get(ImportExcelEnum.NAME.getIndex());
            Integer nation = StringUtils.isBlank(item.get(ImportExcelEnum.NATION.getIndex())) ? null : NationEnum.getCode(item.get(ImportExcelEnum.NATION.getIndex()));
            Date birthday;
            if (StringUtils.isNotBlank(idCard) && !IdcardUtil.isValidCard(idCard)) {
                throw new BusinessException("身份证异常");
            }
            try {
                birthday = StringUtils.isBlank(item.get(ImportExcelEnum.BIRTHDAY.getIndex())) ? IdCardUtil.getBirthDay(item.get(ImportExcelEnum.ID_CARD.getIndex())) : DateFormatUtil.parseDate(item.get(ImportExcelEnum.BIRTHDAY.getIndex()), DateFormatUtil.FORMAT_ONLY_DATE2);
            } catch (ParseException e) {
                throw new BusinessException("学生姓名为:" + studentName + "日期转换异常");
            }
            if (StringUtils.isNoneBlank(idCard, passport)) {
                passport = null;
            }
            // 班级年级信息
            TwoTuple<Integer, Integer> gradeClassInfo = schoolStudentExcelImportService.getSchoolStudentClassInfo(schoolId, schoolGradeMaps, gradeName, className);
            Integer gradeType = GradeCodeEnum.getByName(gradeName).getType();
            // 是否带筛查编码一起上传
            if (StringUtils.isBlank(screeningCode)) {
                notScrenningCodeUpload(userId, existPlanStudentIdCardMap, existPlanStudentPassportMap, existManagementStudentIdCardMap, existManagementStudentPassportMap, noScreeningCodeManagementStudentList, idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType);
                continue;
            }
            // 检查筛查编码是否存在
            if (!existScreeningCode.contains(Long.valueOf(screeningCode))) {
                throw new BusinessException("上传失败：筛查编码在计划中不存在");
            }
            // 是否带着证件号一起上传
            if (StringUtils.isAllBlank(idCard, passport)) {
                notPaperworkUpload(existPlanStudentScreeningCodeMap, virtualStudentList, screeningCode, idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo);
                continue;
            }
            // 筛查编码是否绑定身份证或护照
            ScreeningPlanSchoolStudent planSchoolStudent = existPlanStudentScreeningCodeMap.get(Long.valueOf(screeningCode));
            // 筛查编码没绑定证件号
            if (StringUtils.isAllBlank(planSchoolStudent.getIdCard(), planSchoolStudent.getPassport())) {
                notBindPaperworkUpload(userId, existManagementStudentIdCardMap, existManagementStudentPassportMap, noPaperworkHaveStudentPlanStudents, noPaperworkStudents, noPaperworkPlanStudents, idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, planSchoolStudent);
                continue;
            }
            havePaperworkUpload(userId, existPlanStudentIdCardMap, existPlanStudentPassportMap, existManagementStudentIdCardMap, existManagementStudentPassportMap, havePaperworkPlanStudent, havePaperworkStudent, idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, planSchoolStudent);
        }
        updateOrSaveStudentInfo(screeningPlan, school, existPlanStudentIdCardMap, existPlanStudentPassportMap, noScreeningCodeManagementStudentList, havePaperworkPlanStudent, havePaperworkStudent, noPaperworkHaveStudentPlanStudents, noPaperworkStudents, virtualStudentList, noPaperworkPlanStudents);
    }

    private void havePaperworkUpload(Integer userId, Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap, Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap, Map<String, Student> existManagementStudentIdCardMap, Map<String, Student> existManagementStudentPassportMap, List<ScreeningPlanSchoolStudent> havePaperworkPlanStudent, List<Student> havePaperworkStudent, String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, Integer gradeType, ScreeningPlanSchoolStudent planSchoolStudent) {
        // 判断绑定的证件号是否一致
        checkPaperworkInfo(idCard, passport, planSchoolStudent);
        // 更新学生和筛查学生
        TwoTuple<Student, ScreeningPlanSchoolStudent> twoTuple = getStudentAndPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport);
        Student student = twoTuple.getFirst();
        packageManagementStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, userId, student);

        ScreeningPlanSchoolStudent planStudent = twoTuple.getSecond();
        packagePlanStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, planStudent);

        havePaperworkStudent.add(student);
        havePaperworkPlanStudent.add(planStudent);
    }

    private void notBindPaperworkUpload(Integer userId, Map<String, Student> existManagementStudentIdCardMap, Map<String, Student> existManagementStudentPassportMap, List<ScreeningPlanSchoolStudent> noPaperworkHaveStudentPlanStudents, List<Student> noPaperworkStudents, List<ScreeningPlanSchoolStudent> noPaperworkPlanStudents, String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, Integer gradeType, ScreeningPlanSchoolStudent planSchoolStudent) {
        // 是否在系统中存在
        Student student = getStudent(existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport);
        packagePlanStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, planSchoolStudent);
        // 不存在
        if (Objects.isNull(student.getId())) {
            packageManagementStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, userId, student);
            noPaperworkStudents.add(student);
            noPaperworkPlanStudents.add(planSchoolStudent);
        }
        // 已经存在，不更新多端学生
        planSchoolStudent.setStudentId(student.getId());
        noPaperworkHaveStudentPlanStudents.add(planSchoolStudent);
    }

    private void notPaperworkUpload(Map<Long, ScreeningPlanSchoolStudent> existPlanStudentScreeningCodeMap, List<ScreeningPlanSchoolStudent> virtualStudentList, String screeningCode, String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo) {
        ScreeningPlanSchoolStudent planSchoolStudent = existPlanStudentScreeningCodeMap.get(Long.valueOf(screeningCode));
        packagePlanStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, planSchoolStudent);
        virtualStudentList.add(planSchoolStudent);
    }

    private void notScrenningCodeUpload(Integer userId, Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap, Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap, Map<String, Student> existManagementStudentIdCardMap, Map<String, Student> existManagementStudentPassportMap, List<Student> noScreeningCodeManagementStudentList, String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, Integer gradeType) {
        if (StringUtils.isAllBlank(idCard, passport)) {
            throw new BusinessException("上传失败：身份证、护照信息异常-0001");
        }
        TwoTuple<Student, ScreeningPlanSchoolStudent> twoTuple = getStudentAndPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport);
        Student student = twoTuple.getFirst();
        packageManagementStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, userId, student);
        noScreeningCodeManagementStudentList.add(student);
    }

    /**
     * 判断绑定的证件号是否一致
     *
     * @param idCard            身份证
     * @param passport          护照
     * @param planSchoolStudent 筛查学生
     */
    private void checkPaperworkInfo(String idCard, String passport, ScreeningPlanSchoolStudent planSchoolStudent) {
        if ((StringUtils.isNoneBlank(idCard, planSchoolStudent.getIdCard()) && !StringUtils.equals(idCard, planSchoolStudent.getIdCard()))
                || StringUtils.isNoneBlank(passport, planSchoolStudent.getPassport()) && !StringUtils.equals(passport, planSchoolStudent.getPassport())) {
            throw new BusinessException("上传失败：系统绑定的证件号与上传的不一致");
        }
        if (StringUtils.isNoneBlank(idCard, planSchoolStudent.getPassport()) || StringUtils.isNoneBlank(passport, planSchoolStudent.getIdCard())) {
            throw new BusinessException("上传失败：系统绑定的证件号与上传的不一致");
        }
    }

    private void updateOrSaveStudentInfo(ScreeningPlan screeningPlan, School school, Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap, Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap, List<Student> noScreeningCodeManagementStudentList, List<ScreeningPlanSchoolStudent> havePaperworkPlanStudent, List<Student> havePaperworkStudent, List<ScreeningPlanSchoolStudent> noPaperworkHaveStudentPlanStudents, List<Student> noPaperworkStudents, List<ScreeningPlanSchoolStudent> virtualStudentList, List<ScreeningPlanSchoolStudent> noPaperworkPlanStudents) {
        if (!CollectionUtils.isEmpty(noScreeningCodeManagementStudentList)) {
            saveStudentAndPlanStudent(noScreeningCodeManagementStudentList, existPlanStudentIdCardMap, existPlanStudentPassportMap, screeningPlan, school);
        }
        if (!CollectionUtils.isEmpty(noPaperworkStudents)) {
            studentService.saveOrUpdateBatch(noPaperworkStudents);
            TwoTuple<Map<String, Student>, Map<String, Student>> groupingStudentMap = groupingByIdCardAndPassport(noPaperworkStudents);
            Map<String, Student> idCardMap = groupingStudentMap.getFirst();
            Map<String, Student> passportMap = groupingStudentMap.getSecond();
            for (ScreeningPlanSchoolStudent planSchoolStudent : noPaperworkPlanStudents) {
                Student student = getStudent(idCardMap, passportMap, planSchoolStudent.getIdCard(), planSchoolStudent.getPassport());
                if (Objects.isNull(student.getId())) {
                    throw new BusinessException("数据异常");
                }
                planSchoolStudent.setStudentId(student.getId());
            }
            screeningPlanSchoolStudentService.saveOrUpdateBatch(noPaperworkPlanStudents);
        }
        if (!CollectionUtils.isEmpty(noPaperworkHaveStudentPlanStudents)) {
            screeningPlanSchoolStudentService.saveOrUpdateBatch(noPaperworkHaveStudentPlanStudents);
        }
        if (!CollectionUtils.isEmpty(virtualStudentList)) {
            screeningPlanSchoolStudentService.saveOrUpdateBatch(virtualStudentList);
        }
        if (!CollectionUtils.isEmpty(havePaperworkPlanStudent)) {
            screeningPlanSchoolStudentService.saveOrUpdateBatch(havePaperworkPlanStudent);
        }
        if (!CollectionUtils.isEmpty(havePaperworkStudent)) {
            studentService.saveOrUpdateBatch(havePaperworkStudent);
        }
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
    private void saveStudentAndPlanStudent(List<Student> managementStudentList, Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap,
                                           Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap, ScreeningPlan plan, School school) {
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
            planStudent.setGradeType(student.getGradeType());
            planStudent.setSchoolDistrictId(school.getDistrictId());
            planStudent.setPlanDistrictId(plan.getDistrictId());
            packagePlanStudentByStudent(student, planStudent);
            list.add(planStudent);
        });
        screeningPlanSchoolStudentService.saveOrUpdateBatch(list);
    }

    /**
     * 设置多端学生
     */
    private void packageManagementStudent(String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, Integer gradeType, Integer userId, Student student) {
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
    }

    /**
     * 设置计划学生
     */
    private void packagePlanStudent(String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, ScreeningPlanSchoolStudent planStudent) {
        planStudent.setIdCard(idCard);
        planStudent.setPassport(passport);
        planStudent.setStudentName(studentName);
        planStudent.setBirthday(birthday);
        planStudent.setGender(gender);
        planStudent.setNation(nation);
        planStudent.setGradeId(gradeClassInfo.getFirst());
        planStudent.setClassId(gradeClassInfo.getSecond());
        planStudent.setStudentNo(sno);
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
            if (ObjectsUtil.hasNull(item.getOrDefault(ImportExcelEnum.NAME.getIndex(), null),
                    item.getOrDefault(ImportExcelEnum.GRADE.getIndex(), null),
                    item.getOrDefault(ImportExcelEnum.CLASS.getIndex(), null))) {
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
    private TwoTuple<Student, ScreeningPlanSchoolStudent> getStudentAndPlanStudent(Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap,
                                                                                   Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap,
                                                                                   Map<String, Student> existManagementStudentIdCardMap,
                                                                                   Map<String, Student> existManagementStudentPassportMap,
                                                                                   String idCard,
                                                                                   String passport) {

        return new TwoTuple<>(getStudent(existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport),
                getPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, idCard, passport));
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
    private Student getStudent(Map<String, Student> existManagementStudentIdCardMap,
                               Map<String, Student> existManagementStudentPassportMap,
                               String idCard,
                               String passport) {
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
    private ScreeningPlanSchoolStudent getPlanStudent(Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap,
                                                      Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap,
                                                      String idCard,
                                                      String passport) {
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

}
