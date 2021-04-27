package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.framework.core.util.*;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.constant.ImportExcelEnum;
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
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        Map<String, List<Long>> districtNameCodeMap = new HashMap<>(16);
        //3. 根据上传的筛查学生数据组装基础信息
        genBaseInfoFromUploadData(listMap, idCardSet, gradeNameSet, gradeClassNameSet, districtNameCodeMap);
        Map<Boolean, List<ScreeningPlanSchoolStudent>> alreadyExistOrNotStudents = screeningPlanSchoolStudentService.getByScreeningPlanIdAndSchoolId(screeningPlan.getId(), schoolId).stream().collect(Collectors.groupingBy(planStudent -> idCardSet.contains(planStudent.getIdCard())));
        Map<String, Integer> gradeNameIdMap = schoolGradeService.getBySchoolId(schoolId).stream().collect(Collectors.toMap(SchoolGrade::getName, SchoolGrade::getId));
        Map<String, Integer> gradeClassNameClassIdMap = schoolClassService.getVoBySchoolId(schoolId).stream().collect(Collectors.toMap(schoolClass -> String.format("%s-%s", schoolClass.getGradeName(), schoolClass.getName()), SchoolClass::getId));
        //4. 校验上传筛查学生数据是否合法
        checkExcelDataLegal(idCardSet, snoList, gradeNameSet, gradeClassNameSet, gradeNameIdMap, gradeClassNameClassIdMap, alreadyExistOrNotStudents.get(false));
        //5. 根据身份证号分批获取已有的学生
        Map<String, Student> idCardExistStudents = studentService.getByIdCards(new ArrayList<>(idCardSet)).stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        //6. 获取已有的筛查学生数据
        Map<String, ScreeningPlanSchoolStudent> idCardExistScreeningStudents = CollectionUtils.isEmpty(alreadyExistOrNotStudents.get(true)) ? Collections.emptyMap() : alreadyExistOrNotStudents.get(true).stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getIdCard, Function.identity()));
        List<StudentDTO> excelStudents = getStudentListFromExcelItem(listMap, gradeNameIdMap, gradeClassNameClassIdMap, districtNameCodeMap, school.getSchoolNo());
        Map<String, StudentDTO> excelIdCardStudentMap = excelStudents.stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        //7. 新增或更新学生和筛查学生数据
        addOrUpdateStudentAndScreeningStudent(userId, screeningPlan, schoolId, school, idCardExistStudents, idCardExistScreeningStudents, excelStudents, excelIdCardStudentMap);
    }

    /**
     * 根据上传的筛查学生数据组装基础信息
     *
     * @param listMap
     * @param idCardSet
     * @param gradeNameSet
     * @param gradeClassNameSet
     * @param districtNameCodeMap
     */
    private void genBaseInfoFromUploadData(List<Map<Integer, String>> listMap, Set<String> idCardSet, Set<String> gradeNameSet, Set<String> gradeClassNameSet, Map<String, List<Long>> districtNameCodeMap) {
        listMap.forEach(item -> {
            String gradeName = item.getOrDefault(ImportExcelEnum.GRADE.getIndex(), null);
            String className = item.getOrDefault(ImportExcelEnum.CLASS.getIndex(), null);
            String idCard = item.getOrDefault(ImportExcelEnum.ID_CARD.getIndex(), null);
            String provinceName = item.getOrDefault(ImportExcelEnum.PROVINCE.getIndex(), null);
            String cityName = item.getOrDefault(ImportExcelEnum.CITY.getIndex(), null);
            String areaName = item.getOrDefault(ImportExcelEnum.AREA.getIndex(), null);
            String townName = item.getOrDefault(ImportExcelEnum.TOWN.getIndex(), null);
            idCardSet.add(idCard);
            gradeNameSet.add(gradeName);
            gradeClassNameSet.add(String.format("%s-%s", gradeName, className));
            if (StringUtils.allHasLength(provinceName, cityName, areaName, townName)) {
                districtNameCodeMap.put(String.format("%s-%s-%s-%s", provinceName, cityName, areaName, townName), districtService.getCodeByName(provinceName, cityName, areaName, townName));
            }
        });
    }

    /**
     * 新增或更新学生和筛查学生数据
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
                map.getOrDefault(ImportExcelEnum.CLASS.getIndex(), null),
                map.getOrDefault(ImportExcelEnum.STUDENT_NO.getIndex(), null),
                map.getOrDefault(ImportExcelEnum.ID_CARD.getIndex(), null)))) {
            throw new BusinessException("存在必填项无填写");
        }

        List<String> idCardLists = listMap.stream().map(map -> map.get(ImportExcelEnum.ID_CARD.getIndex())).distinct().collect(Collectors.toList());
        if (idCardLists.size() != listMap.size()) {
            throw new BusinessException("身份证号码存在重复");
        }

        List<String> studentNoList = listMap.stream().map(map -> map.get(ImportExcelEnum.STUDENT_NO.getIndex())).distinct().collect(Collectors.toList());
        if (studentNoList.size() != listMap.size()) {
            throw new BusinessException("学号存在重复");
        }
        return studentNoList;
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
                        .setScreeningOrgId(screeningPlan.getScreeningOrgId()).setPlanDistrictId(screeningPlan.getDistrictId()).setSchoolDistrictId(school.getDistrictId()).setSchoolId(schoolId).setSchoolName(school.getName()).setSchoolNo(school.getSchoolNo()).setStudentId(dbStudent.getId());
            }
            existPlanStudent.setId(existPlanStudent.getId()).setStudentName(student.getName()).setGradeId(student.getGradeId()).setGradeName(student.getGradeName())
                    .setGradeType(GradeCodeEnum.getByName(student.getGradeName()).getType()).setClassId(student.getClassId()).setClassName(student.getClassName())
                    .setBirthday(student.getBirthday()).setGender(student.getGender()).setStudentAge(AgeUtil.countAge(student.getBirthday()))
                    .setStudentSituation(SerializationUtil.serializeWithoutException(dbStudent)).setStudentNo(student.getSno()).setNation(student.getNation())
                    .setProvinceCode(student.getProvinceCode()).setCityCode(student.getCityCode()).setAreaCode(student.getAreaCode())
                    .setTownCode(student.getTownCode()).setAddress(student.getAddress()).setParentPhone(student.getParentPhone());
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
                ScreeningPlanSchoolStudent planSchoolStudent = planSchoolStudentMaps.getOrDefault(idCard, new ScreeningPlanSchoolStudent());
                BeanUtils.copyProperties(student, updateStudent);
                updateStudent.setName(excelStudent.getName()).setSchoolNo(excelStudent.getSchoolNo()).setGender(excelStudent.getGender())
                        .setBirthday(excelStudent.getBirthday()).setNation(ObjectsUtil.getDefaultIfNull(excelStudent.getNation(), student.getNation()))
                        .setGradeId(excelStudent.getGradeId()).setGradeType(GradeCodeEnum.getByName(excelStudent.getGradeName()).getType())
                        .setClassId(excelStudent.getClassId()).setSno(excelStudent.getSno())
                        .setProvinceCode(ObjectsUtil.getDefaultIfNull(excelStudent.getProvinceCode(), student.getProvinceCode()))
                        .setCityCode(ObjectsUtil.getDefaultIfNull(excelStudent.getCityCode(), student.getCityCode()))
                        .setAreaCode(ObjectsUtil.getDefaultIfNull(excelStudent.getAreaCode(), student.getAreaCode()))
                        .setTownCode(ObjectsUtil.getDefaultIfNull(excelStudent.getTownCode(), student.getTownCode()))
                        .setAddress(StringUtils.getDefaultIfBlank(excelStudent.getAddress(), student.getAddress()))
                        .setParentPhone(StringUtils.getDefaultIfBlank(excelStudent.getParentPhone(), student.getParentPhone()));
                updateStudents.add(updateStudent);
                BeanUtils.copyProperties(updateStudent, planSchoolStudent);
                planSchoolStudent.setStudentNo(updateStudent.getSno());
                planSchoolStudent.setStudentName(updateStudent.getName());
                planSchoolStudent.setStudentId(updateStudent.getId());
                updatePlanStudent.add(planSchoolStudent);
            }
        });
        studentService.updateBatchById(updateStudents);
        screeningPlanSchoolStudentService.updateBatchById(updatePlanStudent);
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
     * @param idCardList
     * @param snoList
     * @param gradeNameSet
     * @param gradeClassNameSet
     * @param gradeNameIdMap
     * @param gradeClassNameClassIdMap
     * @param notUploadStudents        已有筛查学生数据中，身份证不在这次上传的数据中的筛查学生
     */
    private void checkExcelDataLegal(Set<String> idCardList, List<String> snoList, Set<String> gradeNameSet, Set<String> gradeClassNameSet, Map<String, Integer> gradeNameIdMap, Map<String, Integer> gradeClassNameClassIdMap, List<ScreeningPlanSchoolStudent> notUploadStudents) {
        // 身份证号是否符合规则
        if (!idCardList.stream().allMatch(CommonValidator::isIdCard)) {
            throw new BusinessException("存在不正确的身份证号");
        }
        // 年级名是否都存在
        if (gradeNameSet.stream().anyMatch(gradeName -> StringUtils.isEmpty(gradeName) || !gradeNameIdMap.keySet().contains(gradeName))) {
            throw new BusinessException("存在不正确的年级名称");
        }
        // 班级名是否都存在
        if (gradeClassNameSet.stream().anyMatch(gradeClassName -> StringUtils.isEmpty(gradeClassName) || !gradeClassNameClassIdMap.keySet().contains(gradeClassName))) {
            throw new BusinessException("存在不正确的班级名称");
        }
        // 上传的学号与已有的学号校验

        List<String> notUploadSno = CollectionUtils.isEmpty(notUploadStudents) ? Collections.emptyList() : notUploadStudents.stream().map(ScreeningPlanSchoolStudent::getStudentNo).collect(Collectors.toList());
        if (CollectionUtils.hasLength(CompareUtil.getRetain(snoList, notUploadSno))) {
            throw new BusinessException("上传数据与已有筛查学生有学号存在重复");
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
    private List<StudentDTO> getStudentListFromExcelItem(List<Map<Integer, String>> listMap, Map<String, Integer> gradeNameIdMap, Map<String, Integer> gradeClassNameClassIdMap, Map<String, List<Long>> districtNameCodeMap, String schoolNo) {
        // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
        List<StudentDTO> excelStudents = listMap.stream().map(item -> {
            try {
                return generateStudentByExcelItem(item, gradeNameIdMap, gradeClassNameClassIdMap, districtNameCodeMap, schoolNo);
            } catch (Exception e) {
                log.error("导入筛查学生数据异常", e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
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
    private StudentDTO generateStudentByExcelItem(Map<Integer, String> item, Map<String, Integer> gradeNameIdMap, Map<String, Integer> gradeClassNameClassIdMap, Map<String, List<Long>> districtNameCodeMap, String schoolNo) {
        try {
            // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
            StudentDTO student = new StudentDTO();
            student.setGradeName(item.get(ImportExcelEnum.GRADE.getIndex())).setClassName(item.get(ImportExcelEnum.CLASS.getIndex()));
            student.setName(StringUtils.getDefaultIfBlank(item.get(ImportExcelEnum.NAME.getIndex()), null))
                    .setGender(StringUtils.isBlank(item.get(ImportExcelEnum.GENDER.getIndex())) ? null : GenderEnum.getType(item.get(ImportExcelEnum.GENDER.getIndex())))
                    .setBirthday(StringUtils.isBlank(item.get(ImportExcelEnum.BIRTHDAY.getIndex())) ? null : com.wupol.myopia.base.util.DateFormatUtil.parseDate(item.get(ImportExcelEnum.BIRTHDAY.getIndex()), DateFormatUtil.FORMAT_ONLY_DATE2))
                    .setNation(StringUtils.isBlank(item.get(ImportExcelEnum.NATION.getIndex())) ? null : NationEnum.getCode(item.get(ImportExcelEnum.NATION.getIndex())))
                    .setSchoolNo(schoolNo)
                    .setGradeId(gradeNameIdMap.get(item.get(ImportExcelEnum.GRADE.getIndex())))
                    .setClassId(gradeClassNameClassIdMap.get(String.format("%s-%s", item.get(ImportExcelEnum.GRADE.getIndex()), item.get(ImportExcelEnum.CLASS.getIndex()))))
                    .setSno(StringUtils.getDefaultIfBlank(item.get(ImportExcelEnum.STUDENT_NO.getIndex()), null))
                    .setIdCard(StringUtils.getDefaultIfBlank(item.get(ImportExcelEnum.ID_CARD.getIndex()), null))
                    .setParentPhone(StringUtils.getDefaultIfBlank(item.get(ImportExcelEnum.PHONE.getIndex()), null))
                    .setAddress(StringUtils.getDefaultIfBlank(item.get(ImportExcelEnum.ADDRESS.getIndex()), null));
            String provinceName = item.getOrDefault(ImportExcelEnum.PROVINCE.getIndex(), null);
            String cityName = item.getOrDefault(ImportExcelEnum.CITY.getIndex(), null);
            String areaName = item.getOrDefault(ImportExcelEnum.AREA.getIndex(), null);
            String townName = item.getOrDefault(ImportExcelEnum.TOWN.getIndex(), null);
            if (StringUtils.allHasLength(provinceName, cityName, areaName, townName)) {
                List<Long> codeList = districtNameCodeMap.get(String.format("%s-%s-%s-%s", provinceName, cityName, areaName, townName));
                if (CollectionUtils.hasLength(codeList)) {
                    student.setProvinceCode(codeList.get(0)).setCityCode(codeList.get(1)).setAreaCode(codeList.get(2)).setTownCode(codeList.get(3));
                }
            }
            return student;
        } catch (Exception e) {
            throw new BusinessException("学生数据有误，请检查", e);
        }
    }


}
