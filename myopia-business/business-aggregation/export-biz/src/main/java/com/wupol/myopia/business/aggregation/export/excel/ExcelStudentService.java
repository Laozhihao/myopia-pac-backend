package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.date.DateUtil;
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
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
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
    @Autowired
    private ScreeningPlanService screeningPlanService;

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
        Map<String, Student> existManagementStudentIdCardMap = existManagementStudentList.stream().filter(s -> StringUtils.isNotBlank(s.getIdCard())).collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        Map<String, Student> existManagementStudentPassportMap = existManagementStudentList.stream().filter(s -> StringUtils.isNotBlank(s.getPassport())).collect(Collectors.toMap(Student::getPassport, Function.identity()));

        // 检查数据中是否有重复数据
        CommonCheck.checkHaveDuplicate(idCardList, snoList, passportList);

        // 获取班级信息
        Map<Integer, List<SchoolGradeExportDTO>> schoolGradeMaps = schoolGradeService.getGradeAndClassMap(Lists.newArrayList(school.getId()));

        List<Student> managementStudentList = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> planStudentList2 = new ArrayList<>();
        List<Student> managementStudentList2 = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> planStudentList3 = new ArrayList<>();
        List<Student> managementStudentList3 = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> virtualStudentList = new ArrayList<>();

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
                if (StringUtils.isAllBlank(idCard, passport)) {
                    throw new BusinessException("上传失败：身份证、护照信息异常-0001");
                }
                TwoTuple<Student, ScreeningPlanSchoolStudent> twoTuple = getStudentAndPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport);
                Student student = twoTuple.getFirst();
                packageManagementStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, student);
                managementStudentList.add(student);
            } else {

                // 检查筛查编码是否存在
                if (!existScreeningCode.contains(Long.valueOf(screeningCode))) {
                    throw new BusinessException("上传失败：筛查编码在计划中不存在");
                }

                // 是否带着证件号一起上传
                if (ObjectsUtil.allNull(idCard, passport)) {
                    ScreeningPlanSchoolStudent planSchoolStudent = existPlanStudentScreeningCodeMap.get(Long.valueOf(screeningCode));
                    packagePlanStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, planSchoolStudent);
                    virtualStudentList.add(planSchoolStudent);
                } else {
                    // 筛查编码是否绑定身份证或护照
                    ScreeningPlanSchoolStudent planSchoolStudent = existPlanStudentScreeningCodeMap.get(Long.valueOf(screeningCode));

                    // 筛查编码没绑定证件号
                    if (ObjectsUtil.allNotNull(planSchoolStudent.getIdCard(), planSchoolStudent.getPassport())) {

                        // 是否在系统中存在
                        Student student = getStudent(existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport);
                        // 不存在
                        if (Objects.isNull(student.getId())) {
                            packageManagementStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, student);
                            managementStudentList3.add(student);
                        } else {
                            // 已经存在，不更新多端学生
                            packagePlanStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, planSchoolStudent);
                            planSchoolStudent.setStudentId(student.getId());
                            planStudentList3.add(planSchoolStudent);
                        }
                    } else {
                        // 判断绑定的证件号是否一致
                        if ((StringUtils.isNoneBlank(idCard, planSchoolStudent.getIdCard()) && !StringUtils.equals(idCard, planSchoolStudent.getIdCard()))
                                || StringUtils.isNoneBlank(passport, planSchoolStudent.getPassport()) && !StringUtils.equals(passport, planSchoolStudent.getPassport())) {
                            throw new BusinessException("上传失败：系统绑定的证件号与上传的不一致");
                        }
                        // 更新学生和筛查学生
                        TwoTuple<Student, ScreeningPlanSchoolStudent> twoTuple = getStudentAndPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport);
                        Student student = twoTuple.getFirst();
                        packageManagementStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, gradeType, student);

                        ScreeningPlanSchoolStudent planStudent = twoTuple.getSecond();
                        packagePlanStudent(idCard, passport, sno, gender, studentName, nation, birthday, gradeClassInfo, planStudent);

                        managementStudentList2.add(student);
                        planStudentList2.add(planStudent);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(managementStudentList)) {
            abc(managementStudentList, existPlanStudentIdCardMap, existPlanStudentPassportMap, screeningPlan, school);
        }

        if (!CollectionUtils.isEmpty(managementStudentList3)) {
            abc(managementStudentList3, existPlanStudentIdCardMap, existPlanStudentPassportMap, screeningPlan, school);
        }

        if (!CollectionUtils.isEmpty(planStudentList3)) {
            screeningPlanSchoolStudentService.saveOrUpdateBatch(planStudentList3);
        }

        if (!CollectionUtils.isEmpty(virtualStudentList)) {
            screeningPlanSchoolStudentService.saveOrUpdateBatch(virtualStudentList);
        }
        if (!CollectionUtils.isEmpty(planStudentList2)) {
            screeningPlanSchoolStudentService.saveOrUpdateBatch(planStudentList2);
        }
        if (!CollectionUtils.isEmpty(managementStudentList2)) {
            studentService.saveOrUpdateBatch(managementStudentList2);
        }
    }

    private void abc(List<Student> managementStudentList, Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap,
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
            packagePlanStudent2(student, planStudent);
            list.add(planStudent);
        });
        screeningPlanSchoolStudentService.saveOrUpdateBatch(list);
    }

    private void packageManagementStudent(String idCard, String passport, String sno, Integer gender, String studentName, Integer nation, Date birthday, TwoTuple<Integer, Integer> gradeClassInfo, Integer gradeType, Student student) {
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
    }

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

    private void packagePlanStudent2(Student student, ScreeningPlanSchoolStudent planStudent) {
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
     * @param school
     * @param listMap
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
     * 检查学生信息是否完成
     *
     * @param student 学生信息
     */
    private void checkStudentInfo(StudentDTO student) {
        if (student.checkBirthdayExceedLimit()) {
            getErrorMsg(student.getIdCard(), student.getScreeningCode(), "出生日期超过范围");
        }
        if (StringUtils.isBlank(student.getName())) {
            getErrorMsg(student.getIdCard(), student.getScreeningCode(), "姓名不能为空");
        }
        if (Objects.isNull(student.getClassId())) {
            getErrorMsg(student.getIdCard(), student.getScreeningCode(), "班级信息为空");
        }
        if (Objects.isNull(student.getGradeId())) {
            getErrorMsg(student.getIdCard(), student.getScreeningCode(), "年级信息为空");
        }
    }

    /**
     * 抛出异常信息
     *
     * @param idCard        学生证
     * @param screeningCode 编码
     * @param message       错误信息
     */
    private void getErrorMsg(String idCard, Long screeningCode, String message) {
        if (StringUtils.isNotBlank(idCard)) {
            throw new BusinessException("学生身份证为:" + idCard + message);
        }
        if (Objects.nonNull(screeningCode)) {
            throw new BusinessException("学生编码为:" + screeningCode + message);
        }
    }

    private TwoTuple<Student, ScreeningPlanSchoolStudent> getStudentAndPlanStudent(Map<String, ScreeningPlanSchoolStudent> existPlanStudentIdCardMap,
                                                                                   Map<String, ScreeningPlanSchoolStudent> existPlanStudentPassportMap,
                                                                                   Map<String, Student> existManagementStudentIdCardMap,
                                                                                   Map<String, Student> existManagementStudentPassportMap,
                                                                                   String idCard,
                                                                                   String passport) {

        return new TwoTuple<>(getStudent(existManagementStudentIdCardMap, existManagementStudentPassportMap, idCard, passport),
                getPlanStudent(existPlanStudentIdCardMap, existPlanStudentPassportMap, idCard, passport));
    }

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
