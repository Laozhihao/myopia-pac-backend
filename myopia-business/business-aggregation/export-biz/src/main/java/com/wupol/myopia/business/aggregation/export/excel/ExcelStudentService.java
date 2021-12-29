package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.util.IdcardUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.IdCardUtil;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.util.AgeUtil;
import com.wupol.myopia.business.common.utils.util.SerializationUtil;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
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
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningCodeGenerator;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

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
    private SchoolClassService schoolClassService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private SchoolStudentService schoolStudentService;

    /**
     * 年级-班级 格式化
     */
    private static final String GRADE_CLASS_NAME_FORMAT = "%s-%s";

    /**
     * 省-市-区-镇 格式化
     */
    private static final String DISTRICT_NAME_FORMAT = "%s-%s-%s-%s";

    /**
     * 处理上传的筛查学生数据
     *
     * @param listMap
     */
    public void insertByUpload(Integer userId, List<Map<Integer, String>> listMap, ScreeningPlan screeningPlan, Integer schoolId) {
        School school = schoolService.getById(schoolId);
        //1. 校验学校是否存在，表格中必填项是否都有
        List<String> snoList = checkSchoolAndNeededExistWithReturnSnoList(school, listMap);
        //2. 获取所有身份证号、年级名称、年级班级名称、区域层级Map
        Set<String> idCardSet = new HashSet<>();
        Set<String> gradeNameSet = new HashSet<>();
        Set<String> gradeClassNameSet = new HashSet<>();
        Set<Long> screeningCode = new HashSet<>();
        Map<String, List<Long>> districtNameCodeMap = new HashMap<>(16);
        //3. 根据上传的筛查学生数据组装基础信息
        genBaseInfoFromUploadData(listMap, idCardSet, gradeNameSet, gradeClassNameSet, districtNameCodeMap, screeningCode);
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlan.getId());
        // 通过身份证分组
        Map<Boolean, List<ScreeningPlanSchoolStudent>> alreadyExistOrNotStudents = planSchoolStudentList.stream().collect(Collectors.groupingBy(planStudent -> idCardSet.contains(planStudent.getIdCard())));
        // 通过ScreeningCode分组
        Map<Boolean, List<ScreeningPlanSchoolStudent>> screeningCodeMap = planSchoolStudentList.stream().collect(Collectors.groupingBy(planStudent -> screeningCode.contains(planStudent.getScreeningCode())));
        Map<String, Integer> gradeNameIdMap = schoolGradeService.getBySchoolId(schoolId).stream().collect(Collectors.toMap(SchoolGrade::getName, SchoolGrade::getId));
        Map<String, Integer> gradeClassNameClassIdMap = schoolClassService.getVoBySchoolId(schoolId).stream().collect(Collectors.toMap(schoolClass -> String.format(GRADE_CLASS_NAME_FORMAT, schoolClass.getGradeName(), schoolClass.getName()), SchoolClass::getId));

        //4. 校验上传筛查学生数据是否合法
        checkExcelDataLegal(snoList, gradeNameSet, gradeClassNameSet, gradeNameIdMap, gradeClassNameClassIdMap, alreadyExistOrNotStudents.get(false), screeningCodeMap.get(false), schoolId, idCardSet);
        //5. 根据身份证号分批获取已有的学生
        Map<String, Student> idCardExistStudents = studentService.getByIdCards(new ArrayList<>(idCardSet)).stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        //6. 获取已有的筛查学生数据
        Map<String, ScreeningPlanSchoolStudent> idCardExistScreeningStudents = CollectionUtils.isEmpty(alreadyExistOrNotStudents.get(true)) ?
                Collections.emptyMap() : alreadyExistOrNotStudents.get(true).stream().filter(e -> StringUtils.isNotBlank(e.getIdCard())).collect(Collectors.toMap(ScreeningPlanSchoolStudent::getIdCard, Function.identity()));
        List<StudentDTO> excelStudents = getStudentListFromExcelItem(listMap, gradeNameIdMap, gradeClassNameClassIdMap, districtNameCodeMap, school.getSchoolNo(), schoolId);
        Map<String, StudentDTO> excelIdCardStudentMap = excelStudents.stream()
                .filter(e -> StringUtils.isNotBlank(e.getIdCard()) && Objects.isNull(e.getScreeningCode()))
                .collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        // 7. 新增或更新学生和筛查学生数据(更新只存在身份号码而没筛查编号的学生)
        List<StudentDTO> onlyHaveIdCardList = new ArrayList<>(excelIdCardStudentMap.values());
        addOrUpdateStudentAndScreeningStudent(userId, screeningPlan, schoolId, school, idCardExistStudents, idCardExistScreeningStudents, onlyHaveIdCardList, excelIdCardStudentMap);
        // 8 更新存在筛查编号的学生
        updateMockPlanStudent(excelStudents.stream().filter(e -> Objects.nonNull(e.getScreeningCode())).collect(Collectors.toList()), screeningPlan.getId(), schoolId);
        // 9 更新学校端学生(只处理有身份证和学号的学生)
        updateSchoolStudent(excelStudents.stream()
                        .filter(s -> StringUtils.isNotBlank(s.getIdCard()) && StringUtils.isNotBlank(s.getSno())).collect(Collectors.toList()),
                schoolId, userId);
    }

    /**
     * 根据上传的筛查学生数据组装基础信息
     *
     * @param listMap
     * @param idCardSet
     * @param gradeNameSet
     * @param gradeClassNameSet
     * @param districtNameCodeMap
     * @param screeningCodeSet    筛查编号
     */
    private void genBaseInfoFromUploadData(List<Map<Integer, String>> listMap, Set<String> idCardSet,
                                           Set<String> gradeNameSet, Set<String> gradeClassNameSet,
                                           Map<String, List<Long>> districtNameCodeMap, Set<Long> screeningCodeSet) {
        listMap.forEach(item -> {
            String gradeName = item.getOrDefault(ImportExcelEnum.GRADE.getIndex(), null);
            String className = item.getOrDefault(ImportExcelEnum.CLASS.getIndex(), null);
            String idCard = item.getOrDefault(ImportExcelEnum.ID_CARD.getIndex(), null);
            String provinceName = item.getOrDefault(ImportExcelEnum.PROVINCE.getIndex(), null);
            String cityName = item.getOrDefault(ImportExcelEnum.CITY.getIndex(), null);
            String areaName = item.getOrDefault(ImportExcelEnum.AREA.getIndex(), null);
            String townName = item.getOrDefault(ImportExcelEnum.TOWN.getIndex(), null);
            String screeningCode = item.getOrDefault(ImportExcelEnum.SCREENING_CODE.getIndex(), null);
            idCardSet.add(idCard);
            gradeNameSet.add(gradeName);
            if (StringUtils.isNotBlank(screeningCode)) {
                screeningCodeSet.add(Long.valueOf(screeningCode));
            }
            gradeClassNameSet.add(String.format(GRADE_CLASS_NAME_FORMAT, gradeName, className));
            if (StringUtils.allHasLength(provinceName, cityName, areaName, townName)) {
                districtNameCodeMap.put(String.format(DISTRICT_NAME_FORMAT, provinceName, cityName, areaName, townName), districtService.getCodeByName(provinceName, cityName, areaName, townName));
            }
        });
    }

    /**
     * 新增或更新学生和筛查学生数据
     *
     * @param userId
     * @param screeningPlan
     * @param schoolId
     * @param school
     * @param idCardExistStudents
     * @param idCardExistScreeningStudents
     * @param excelStudents
     * @param excelIdCardStudentMap
     */
    private void addOrUpdateStudentAndScreeningStudent(Integer userId, ScreeningPlan screeningPlan, Integer schoolId, School school, Map<String, Student> idCardExistStudents, Map<String, ScreeningPlanSchoolStudent> idCardExistScreeningStudents, List<StudentDTO> excelStudents, Map<String, StudentDTO> excelIdCardStudentMap) {
        //1. 筛选出需新增的学生并新增
        addStudents(userId, idCardExistStudents, excelIdCardStudentMap);
        //2. 已有的要判断是否需更新
        updateStudents(idCardExistStudents, excelIdCardStudentMap, screeningPlan.getId(), schoolId);
        //3. 处理筛查学生
        addOrUpdateScreeningPlanStudents(screeningPlan, schoolId, school, idCardExistStudents, idCardExistScreeningStudents, excelStudents);
    }

    /**
     * 校验学校是否存在，表格中必填项是否都有
     *
     * @param school
     * @param listMap
     */
    private List<String> checkSchoolAndNeededExistWithReturnSnoList(School school, List<Map<Integer, String>> listMap) {
        if (Objects.isNull(school)) {
            throw new BusinessException("不存在该学校");
        }
        // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
        if (listMap.stream().anyMatch(map -> ObjectsUtil.hasNull(
                map.getOrDefault(ImportExcelEnum.NAME.getIndex(), null),
                map.getOrDefault(ImportExcelEnum.GENDER.getIndex(), null),
                map.getOrDefault(ImportExcelEnum.BIRTHDAY.getIndex(), null),
                map.getOrDefault(ImportExcelEnum.GRADE.getIndex(), null),
                map.getOrDefault(ImportExcelEnum.CLASS.getIndex(), null)))) {
            throw new BusinessException("存在必填项无填写");
        }
        List<String> idCards = listMap.stream().map(map -> map.get(ImportExcelEnum.ID_CARD.getIndex())).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> duplicateIdCard = ListUtil.getDuplicateElements(idCards);
        if (CollectionUtils.isNotEmpty(duplicateIdCard)) {
            throw new BusinessException("身份证" + org.apache.commons.lang3.StringUtils.join(duplicateIdCard, ",") + "重复");
        }

        List<String> studentNos = listMap.stream().map(map -> map.get(ImportExcelEnum.STUDENT_NO.getIndex())).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> duplicateSno = ListUtil.getDuplicateElements(studentNos);
        if (CollectionUtils.isNotEmpty(duplicateSno)) {
            throw new BusinessException("学号" + org.apache.commons.lang3.StringUtils.join(duplicateSno, ",") + "重复");
        }
        return studentNos;
    }

    /**
     * 处理筛查学生
     *
     * @param screeningPlan
     * @param schoolId
     * @param school
     * @param idCardExistStudents
     * @param idCardExistScreeningStudents
     * @param excelStudents
     */
    private void addOrUpdateScreeningPlanStudents(ScreeningPlan screeningPlan, Integer schoolId, School school, Map<String, Student> idCardExistStudents, Map<String, ScreeningPlanSchoolStudent> idCardExistScreeningStudents, List<StudentDTO> excelStudents) {
        List<ScreeningPlanSchoolStudent> addOrUpdatePlanStudents = excelStudents.stream().map(student -> {
            ScreeningPlanSchoolStudent existPlanStudent = idCardExistScreeningStudents.getOrDefault(student.getIdCard(), null);
            Student dbStudent = idCardExistStudents.get(student.getIdCard());
            if (Objects.isNull(existPlanStudent)) {
                existPlanStudent = new ScreeningPlanSchoolStudent();
                existPlanStudent.setIdCard(student.getIdCard()).setSrcScreeningNoticeId(screeningPlan.getSrcScreeningNoticeId()).setScreeningTaskId(screeningPlan.getScreeningTaskId()).setScreeningPlanId(screeningPlan.getId())
                        .setScreeningOrgId(screeningPlan.getScreeningOrgId()).setPlanDistrictId(screeningPlan.getDistrictId()).setSchoolDistrictId(school.getDistrictId()).setSchoolId(schoolId).setSchoolName(school.getName()).setSchoolNo(school.getSchoolNo()).setStudentId(dbStudent.getId())
                        .setScreeningCode(ScreeningCodeGenerator.nextId());
            }
            existPlanStudent.setId(existPlanStudent.getId()).setStudentName(student.getName()).setGradeId(student.getGradeId()).setGradeName(student.getGradeName())
                    .setGradeType(GradeCodeEnum.getByName(student.getGradeName()).getType()).setClassId(student.getClassId()).setClassName(student.getClassName())
                    .setBirthday(student.getBirthday()).setGender(student.getGender()).setStudentAge(AgeUtil.countAge(student.getBirthday()))
                    .setStudentSituation(SerializationUtil.serializeWithoutException(dbStudent)).setStudentNo(student.getSno()).setNation(student.getNation())
                    .setProvinceCode(student.getProvinceCode()).setCityCode(student.getCityCode()).setAreaCode(student.getAreaCode())
                    .setTownCode(student.getTownCode()).setAddress(student.getAddress()).setParentPhone(student.getParentPhone())
                    .setScreeningPlanId(screeningPlan.getId()).setSchoolId(schoolId);
            return existPlanStudent;
        }).collect(Collectors.toList());
        screeningPlanSchoolStudentService.saveOrUpdateBatch(addOrUpdatePlanStudents);
    }

    /**
     * 更新学生数据
     *
     * @param idCardExistStudents
     * @param excelIdCardStudentMap
     * @param screeningPlanId
     * @param schoolId
     */
    private void updateStudents(Map<String, Student> idCardExistStudents, Map<String, StudentDTO> excelIdCardStudentMap, Integer screeningPlanId, Integer schoolId) {
        List<String> needCheckUpdateStudentIdCards = CompareUtil.getRetain(idCardExistStudents.keySet(), excelIdCardStudentMap.keySet());
        List<Student> updateStudents = new ArrayList<>();
        // 查找通过身份证查找计划中的学生
        Map<String, ScreeningPlanSchoolStudent> planSchoolStudentMaps = screeningPlanSchoolStudentService.getByIdCards(screeningPlanId, schoolId, needCheckUpdateStudentIdCards).stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getIdCard, Function.identity()));
        // 需要更新计划中的学生信息
        List<ScreeningPlanSchoolStudent> updatePlanStudent = new ArrayList<>();
        needCheckUpdateStudentIdCards.forEach(idCard -> {
            Student student = idCardExistStudents.get(idCard);
            StudentDTO excelStudent = excelIdCardStudentMap.get(idCard);
            if (student.checkNeedUpdate(excelStudent)) {
                Student updateStudent = new Student();
                BeanUtils.copyProperties(student, updateStudent);
                updateStudent.setName(excelStudent.getName())
                        .setGender(excelStudent.getGender())
                        .setBirthday(excelStudent.getBirthday()).setNation(ObjectsUtil.getDefaultIfNull(excelStudent.getNation(), student.getNation()))
                        .setGradeId(excelStudent.getGradeId()).setGradeType(GradeCodeEnum.getByName(excelStudent.getGradeName()).getType())
                        .setClassId(excelStudent.getClassId()).setSno(excelStudent.getSno())
                        .setSchoolId(schoolId)
                        .setAddress(StringUtils.getDefaultIfBlank(excelStudent.getAddress(), student.getAddress()))
                        .setParentPhone(StringUtils.getDefaultIfBlank(excelStudent.getParentPhone(), student.getParentPhone()));
                updateStudent.setProvinceCode(ObjectsUtil.getDefaultIfNull(excelStudent.getProvinceCode(), student.getProvinceCode()));
                updateStudent.setCityCode(ObjectsUtil.getDefaultIfNull(excelStudent.getCityCode(), student.getCityCode()));
                updateStudent.setAreaCode(ObjectsUtil.getDefaultIfNull(excelStudent.getAreaCode(), student.getAreaCode()));
                updateStudent.setTownCode(ObjectsUtil.getDefaultIfNull(excelStudent.getTownCode(), student.getTownCode()));
                updateStudents.add(updateStudent);
                ScreeningPlanSchoolStudent planSchoolStudent = planSchoolStudentMaps.getOrDefault(idCard, null);
                if (Objects.nonNull(planSchoolStudent)) {
                    Integer id = planSchoolStudent.getId();
                    Date createTime = planSchoolStudent.getCreateTime();
                    BeanUtils.copyProperties(updateStudent, planSchoolStudent);
                    planSchoolStudent.setId(id);
                    // 保留原始创建时间，方便排查问题
                    planSchoolStudent.setCreateTime(createTime);
                    planSchoolStudent.setStudentNo(updateStudent.getSno());
                    planSchoolStudent.setStudentName(updateStudent.getName());
                    planSchoolStudent.setStudentId(updateStudent.getId());
                    updatePlanStudent.add(planSchoolStudent);
                }
            }
        });
        studentService.updateBatchById(updateStudents);
        if (!CollectionUtils.isEmpty(updatePlanStudent)) {
            screeningPlanSchoolStudentService.updateBatchById(updatePlanStudent);
        }
    }

    /**
     * 新增学生数据
     *
     * @param userId
     * @param idCardExistStudents
     * @param excelIdCardStudentMap
     */
    private void addStudents(Integer userId, Map<String, Student> idCardExistStudents, Map<String, StudentDTO> excelIdCardStudentMap) {
        List<String> needAddedIdCards = CompareUtil.getAdded(new ArrayList<>(idCardExistStudents.keySet()), new ArrayList<>(excelIdCardStudentMap.keySet()));
        if (CollectionUtils.hasLength(needAddedIdCards)) {
            List<Student> addedStudent = needAddedIdCards.stream().map(idCard -> {
                Student s = new Student();
                StudentDTO excelStudent = excelIdCardStudentMap.get(idCard);
                BeanUtils.copyProperties(excelStudent, s);
                s.setGradeType(GradeCodeEnum.getByName(excelStudent.getGradeName()).getType());
                return s;
            }).collect(Collectors.toList());
            addedStudent.forEach(student -> student.setCreateUserId(userId));
            studentService.saveOrUpdateBatch(addedStudent);
            addedStudent.forEach(student -> idCardExistStudents.put(student.getIdCard(), student));
        }
    }

    /**
     * 校验excel的筛查学生数据是否正确
     * 1. 身份证号
     * 2. 年级
     * 3. 班级
     *
     * @param snoList
     * @param gradeNameSet
     * @param gradeClassNameSet
     * @param gradeNameIdMap
     * @param gradeClassNameClassIdMap
     * @param notUploadStudents        已有筛查学生数据中，身份证不在这次上传的数据中的筛查学生
     * @param screeningCodeList        筛查CodeList
     * @param idCardSet
     */
    private void checkExcelDataLegal(List<String> snoList, Set<String> gradeNameSet, Set<String> gradeClassNameSet,
                                     Map<String, Integer> gradeNameIdMap, Map<String, Integer> gradeClassNameClassIdMap,
                                     List<ScreeningPlanSchoolStudent> notUploadStudents,
                                     List<ScreeningPlanSchoolStudent> screeningCodeList, Integer schoolId, Set<String> idCardSet) {
        // 年级名是否都存在
        List<String> noLegalGrade = gradeNameSet.stream()
                .filter(gradeName -> StringUtils.isEmpty(gradeName) || !gradeNameIdMap.containsKey(gradeName))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(noLegalGrade)) {
            throw new BusinessException("以下年级名称不正确" + noLegalGrade);
        }

        List<String> notLegalClass = gradeClassNameSet
                .stream().filter(gradeClassName -> StringUtils.isEmpty(gradeClassName) || !gradeClassNameClassIdMap.containsKey(gradeClassName))
                .collect(Collectors.toList());

        // 班级名是否都存在
        if (!CollectionUtils.isEmpty(notLegalClass)) {
            throw new BusinessException("以下班级名称不正确" + notLegalClass);
        }

        List<String> notLegalIdCards = new ArrayList<>();
        if (!CollectionUtils.isEmpty(idCardSet)) {
            for (String s : idCardSet) {
                if (StringUtils.isNotBlank(s) && !IdcardUtil.isValidCard(s)) {
                    notLegalIdCards.add(s);
                }
            }
            if (!CollectionUtils.isEmpty(notLegalIdCards)) {
                throw new BusinessException("身份证格式错误：" + notLegalIdCards);
            }
        }

        // 上传的学号与已有的学号校验(只考虑自己学校)
        List<String> notUploadSno = CollectionUtils.isEmpty(notUploadStudents) ? Collections.emptyList() : notUploadStudents.stream().filter(s -> s.getSchoolId().equals(schoolId)).map(ScreeningPlanSchoolStudent::getStudentNo).collect(Collectors.toList());
        List<String> codeNotUploadSno = CollectionUtils.isEmpty(screeningCodeList) ? Collections.emptyList() : screeningCodeList.stream().filter(s -> s.getSchoolId().equals(schoolId)).map(ScreeningPlanSchoolStudent::getStudentNo).collect(Collectors.toList());
        if (CollectionUtils.hasLength(CompareUtil.getRetain(snoList, notUploadSno))
                && CollectionUtils.hasLength(CompareUtil.getRetain(snoList, codeNotUploadSno))) {
            List<String> result = ListUtils.intersection(snoList, notUploadSno);
            if (CollectionUtils.isEmpty(result)) {
                result = ListUtils.intersection(snoList, codeNotUploadSno);
            }
            throw new BusinessException("上传数据与已有筛查学生有学号存在重复，学号：" + result);
        }
    }


    /**
     * 根据excel数据生成学生数据列表
     *
     * @param listMap
     * @param gradeNameIdMap
     * @param gradeClassNameClassIdMap
     * @param districtNameCodeMap
     * @param schoolNo                 学校编号
     * @return List<Student>
     */
    private List<StudentDTO> getStudentListFromExcelItem(List<Map<Integer, String>> listMap, Map<String, Integer> gradeNameIdMap, Map<String, Integer> gradeClassNameClassIdMap, Map<String, List<Long>> districtNameCodeMap, String schoolNo, Integer schoolId) {
        // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
        List<StudentDTO> excelStudents = listMap.stream().map(item -> {
                    return generateStudentByExcelItem(item, gradeNameIdMap, gradeClassNameClassIdMap, districtNameCodeMap, schoolNo, schoolId);
                    // 过滤身份证和Code同时为空的数据
                }).filter(s -> StringUtils.isNotBlank(s.getIdCard()) || Objects.nonNull(s.getScreeningCode()))
                .collect(Collectors.toList());
        if (excelStudents.size() != listMap.size()) {
            throw new BusinessException("学生数据有误，请检查");
        }
        return excelStudents;
    }

    /**
     * 根据上传筛查学生的表格项生成Student
     *
     * @param item
     * @param gradeNameIdMap
     * @param gradeClassNameClassIdMap
     * @param schoolNo                 学校编号
     * @return 学生实体
     */
    private StudentDTO generateStudentByExcelItem(Map<Integer, String> item, Map<String, Integer> gradeNameIdMap, Map<String, Integer> gradeClassNameClassIdMap, Map<String, List<Long>> districtNameCodeMap, String schoolNo, Integer schoolId) {
        // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
        StudentDTO student = new StudentDTO();
        student.setGradeName(item.get(ImportExcelEnum.GRADE.getIndex()))
                .setClassName(item.get(ImportExcelEnum.CLASS.getIndex()));
        student.setName(StringUtils.getDefaultIfBlank(item.get(ImportExcelEnum.NAME.getIndex()), null))
                .setGender(StringUtils.isBlank(item.get(ImportExcelEnum.GENDER.getIndex())) ? IdCardUtil.getGender(item.get(ImportExcelEnum.ID_CARD.getIndex())) : GenderEnum.getType(item.get(ImportExcelEnum.GENDER.getIndex())))
                .setNation(StringUtils.isBlank(item.get(ImportExcelEnum.NATION.getIndex())) ? null : NationEnum.getCode(item.get(ImportExcelEnum.NATION.getIndex())))
                .setSchoolId(schoolId)
                .setGradeId(gradeNameIdMap.get(item.get(ImportExcelEnum.GRADE.getIndex())))
                .setClassId(gradeClassNameClassIdMap.get(String.format(GRADE_CLASS_NAME_FORMAT, item.get(ImportExcelEnum.GRADE.getIndex()), item.get(ImportExcelEnum.CLASS.getIndex()))))
                .setSno(StringUtils.getDefaultIfBlank(item.get(ImportExcelEnum.STUDENT_NO.getIndex()), null))
                .setIdCard(StringUtils.getDefaultIfBlank(item.get(ImportExcelEnum.ID_CARD.getIndex()), null))
                .setParentPhone(StringUtils.getDefaultIfBlank(item.get(ImportExcelEnum.PHONE.getIndex()), null))
                .setAddress(StringUtils.getDefaultIfBlank(item.get(ImportExcelEnum.ADDRESS.getIndex()), null));
        try {
            student.setBirthday(StringUtils.isBlank(item.get(ImportExcelEnum.BIRTHDAY.getIndex())) ? IdCardUtil.getBirthDay(item.get(ImportExcelEnum.ID_CARD.getIndex())) : DateFormatUtil.parseDate(item.get(ImportExcelEnum.BIRTHDAY.getIndex()), DateFormatUtil.FORMAT_ONLY_DATE2));
        } catch (ParseException e) {
            throw new BusinessException("学生姓名为:" + student.getName() + "日期转换异常");
        }
        String provinceName = item.getOrDefault(ImportExcelEnum.PROVINCE.getIndex(), null);
        String cityName = item.getOrDefault(ImportExcelEnum.CITY.getIndex(), null);
        String areaName = item.getOrDefault(ImportExcelEnum.AREA.getIndex(), null);
        String townName = item.getOrDefault(ImportExcelEnum.TOWN.getIndex(), null);
        String code = item.getOrDefault(ImportExcelEnum.SCREENING_CODE.getIndex(), null);
        student.setScreeningCode(Objects.nonNull(code) ? Long.valueOf(code) : null);
        checkStudentInfo(student);
        if (StringUtils.allHasLength(provinceName, cityName, areaName, townName)) {
            List<Long> codeList = districtNameCodeMap.get(String.format(DISTRICT_NAME_FORMAT, provinceName, cityName, areaName, townName));
            if (CollectionUtils.hasLength(codeList)) {
                student.setProvinceCode(codeList.get(0));
                student.setCityCode(codeList.get(1));
                student.setAreaCode(codeList.get(2));
                student.setTownCode(codeList.get(3));
            }
        }
        return student;
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

    /**
     * 更新学生信息
     *
     * @param excelStudent Excel学生
     */
    private void updateMockPlanStudent(List<StudentDTO> excelStudent, Integer planId, Integer schoolId) {

        List<Long> screeningCodes = excelStudent.stream().map(StudentDTO::getScreeningCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(screeningCodes)) {
            return;
        }
        List<Long> duplicateCode = ListUtil.getDuplicateElements(screeningCodes);
        if (CollectionUtils.isNotEmpty(duplicateCode)) {
            throw new BusinessException("编号为" + org.apache.commons.lang3.StringUtils.join(duplicateCode, ",") + "重复");
        }
        Map<Long, StudentDTO> excelStudentMap = excelStudent.stream().collect(Collectors.toMap(StudentDTO::getScreeningCode, Function.identity()));

        List<ScreeningPlanSchoolStudent> planStudents = screeningPlanSchoolStudentService.getByScreeningCodes(screeningCodes, planId);
        if (CollectionUtils.isEmpty(planStudents) || planStudents.size() != excelStudent.size()) {
            throw new BusinessException("编码数据异常");
        }

        List<Student> studentList = studentService.getByIds(planStudents.stream()
                .map(ScreeningPlanSchoolStudent::getStudentId).collect(Collectors.toList()));
        if (studentList.size() != excelStudent.size()) {
            throw new BusinessException("学生数据异常");
        }

        School school = schoolService.getById(schoolId);

        planStudents.forEach(planStudent -> {
            StudentDTO updateStudent = excelStudentMap.get(planStudent.getScreeningCode());
            planStudent.setStudentName(updateStudent.getName());
            planStudent.setGender(updateStudent.getGender());
            planStudent.setBirthday(updateStudent.getBirthday());
            planStudent.setNation(updateStudent.getNation());
            planStudent.setGradeId(updateStudent.getGradeId());
            planStudent.setGradeName(updateStudent.getGradeName());
            planStudent.setClassId(updateStudent.getClassId());
            planStudent.setClassName(updateStudent.getClassName());
            planStudent.setStudentNo(updateStudent.getSno());
            planStudent.setParentPhone(updateStudent.getParentPhone());
            planStudent.setProvinceCode(updateStudent.getProvinceCode());
            planStudent.setCityCode(updateStudent.getCityCode());
            planStudent.setAreaCode(updateStudent.getAreaCode());
            planStudent.setTownCode(updateStudent.getTownCode());
            planStudent.setAddress(updateStudent.getAddress());
            planStudent.setScreeningPlanId(planId);
            planStudent.setSchoolId(schoolId);
            planStudent.setSchoolName(school.getName());
            planStudent.setIdCard(updateStudent.getIdCard());
        });
        screeningPlanSchoolStudentService.batchUpdateOrSave(planStudents);
        Map<Integer, ScreeningPlanSchoolStudent> planStudentMap = planStudents.stream()
                .collect(Collectors.toMap(ScreeningPlanSchoolStudent::getStudentId, Function.identity()));
        try {
            updateManagementStudent(studentList, planStudentMap, school);
        } catch (DuplicateKeyException e) {
            log.error("身份证重复", e);
            throw new BusinessException("身份证重复数据异常，请检查");
        }

        // 更新筛查结果
        updateStudentResult(planStudents, schoolId);
    }

    /**
     * 更新多端管理学生
     *
     * @param studentList    学生列表
     * @param planStudentMap 计划学生列表
     */
    private void updateManagementStudent(List<Student> studentList, Map<Integer, ScreeningPlanSchoolStudent> planStudentMap, School school) {
        studentList.forEach(student -> {
            ScreeningPlanSchoolStudent planSchoolStudent = planStudentMap.get(student.getId());
            student.setName(planSchoolStudent.getStudentName());
            student.setGender(planSchoolStudent.getGender());
            student.setBirthday(planSchoolStudent.getBirthday());
            student.setNation(planSchoolStudent.getNation());
            student.setGradeId(planSchoolStudent.getGradeId());
            student.setClassId(planSchoolStudent.getClassId());
            student.setSno(planSchoolStudent.getStudentNo());
            student.setParentPhone(planSchoolStudent.getParentPhone());
            student.setProvinceCode(planSchoolStudent.getProvinceCode());
            student.setCityCode(planSchoolStudent.getCityCode());
            student.setAreaCode(planSchoolStudent.getAreaCode());
            student.setTownCode(planSchoolStudent.getTownCode());
            student.setAddress(planSchoolStudent.getAddress());
            student.setIdCard(planSchoolStudent.getIdCard());
        });
        studentService.batchUpdateOrSave(studentList);
    }

    /**
     * 更新筛查结果
     *
     * @param planStudents 筛查学生
     * @param schoolId     学校Id
     */
    private void updateStudentResult(List<ScreeningPlanSchoolStudent> planStudents, Integer schoolId) {
        List<Integer> planStudentIds = planStudents.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toList());
        List<VisionScreeningResult> resultList = visionScreeningResultService.getByPlanStudentIds(planStudentIds);
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        resultList.forEach(r -> r.setSchoolId(schoolId));
        visionScreeningResultService.updateBatchById(resultList);
    }

    /**
     * 更新学校端学生
     *
     * @param excelStudents 导入的学生
     * @param schoolId      学校Id
     */
    private void updateSchoolStudent(List<StudentDTO> excelStudents, Integer schoolId, Integer userId) {

        if (CollectionUtils.isEmpty(excelStudents)) {
            return;
        }

        School school = schoolService.getById(schoolId);
        List<SchoolStudent> allSchoolStudents = schoolStudentService.getBySchoolId(schoolId);

        List<String> idCards = excelStudents.stream().map(Student::getIdCard).collect(Collectors.toList());

        // 获取学校端的学生
        List<SchoolStudent> schoolStudents = schoolStudentService.getByIdCards(idCards, schoolId);
        Map<String, Integer> schoolStudentMap = schoolStudents.stream().collect(Collectors.toMap(SchoolStudent::getIdCard, SchoolStudent::getId));

        // 获取管理端的学生
        List<Student> managementStudents = studentService.getByIdCards(idCards);
        Map<String, Integer> managementStudentMap = managementStudents.stream().collect(Collectors.toMap(Student::getIdCard, Student::getId));

        List<SchoolStudent> saveSchoolStudentList = new ArrayList<>();

        excelStudents.forEach(excelStudent -> {

            String idCard = excelStudent.getIdCard();
            Integer id = schoolStudentMap.get(idCard);
            String sno = excelStudent.getSno();

            // 检查学号是否重复
            checkStudentSno(id, allSchoolStudents, sno);

            // 设置多端管理的Id
            Integer managementStudentId = managementStudentMap.get(idCard);
            if (Objects.isNull(managementStudentId)) {
                throw new BusinessException("身份证为" + idCard + "信息异常");
            }

            SchoolStudent schoolStudent = new SchoolStudent();

            schoolStudent.setId(id)
                    .setStudentId(managementStudentId)
                    .setName(excelStudent.getName())
                    .setGender(excelStudent.getGender())
                    .setBirthday(excelStudent.getBirthday())
                    .setNation(excelStudent.getNation())
                    .setSchoolNo(school.getSchoolNo())
                    .setGradeType(excelStudent.getGradeType())
                    .setSno(sno)
                    .setIdCard(idCard)
                    .setParentPhone(excelStudent.getParentPhone())
                    .setCreateUserId(userId)
                    .setSchoolId(schoolId)
                    .setGradeId(excelStudent.getGradeId())
                    .setGradeName(excelStudent.getGradeName())
                    .setClassId(excelStudent.getClassId())
                    .setClassName(excelStudent.getClassName());
            schoolStudent.setProvinceCode(excelStudent.getProvinceCode());
            schoolStudent.setCityCode(excelStudent.getCityCode());
            schoolStudent.setAreaCode(excelStudent.getAreaCode());
            schoolStudent.setTownCode(excelStudent.getTownCode());
            schoolStudent.setAddress(excelStudent.getAddress());
            saveSchoolStudentList.add(schoolStudent);
        });
        schoolStudentService.saveOrUpdateBatch(saveSchoolStudentList);
    }

    /**
     * 检查学号是否重复
     *
     * @param id                学校学生Id
     * @param allSchoolStudents 所有学生
     * @param sno               学号
     */
    private void checkStudentSno(Integer id, List<SchoolStudent> allSchoolStudents, String sno) {
        List<String> snoList;
        if (Objects.isNull(id)) {
            snoList = allSchoolStudents.stream().map(SchoolStudent::getSno).collect(Collectors.toList());
        } else {
            // 除自己外是否使用过
            snoList = allSchoolStudents.stream().filter(s -> !s.getId().equals(id)).map(SchoolStudent::getSno).collect(Collectors.toList());
        }
        // 学号是否已经使用过
        List<String> retain = ListUtils.retainAll(snoList, Lists.newArrayList(sno));
        if (!CollectionUtils.isEmpty(retain)) {
            throw new BusinessException("学校端学号" + sno + "重复");
        }
    }

}
