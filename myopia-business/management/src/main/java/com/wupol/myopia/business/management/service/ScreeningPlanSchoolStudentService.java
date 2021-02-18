package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.*;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.management.constant.GenderEnum;
import com.wupol.myopia.business.management.constant.NationEnum;
import com.wupol.myopia.business.management.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanSchoolStudentMapper;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.domain.vo.SchoolGradeVo;
import com.wupol.myopia.business.management.util.AgeUtil;
import com.wupol.myopia.business.management.util.SerializationUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.validation.Valid;
import java.lang.reflect.GenericArrayType;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScreeningPlanSchoolStudentService extends BaseService<ScreeningPlanSchoolStudentMapper, ScreeningPlanSchoolStudent> {

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

    /**
     * 根据学生id获取筛查计划学校学生
     *
     * @param studentId 学生ID
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getByStudentId(Integer studentId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningPlanSchoolStudent>().eq("student_id", studentId));
    }

    /**
     * 删除筛查计划中，除了指定学校ID的其它学校学生信息
     *
     * @param screeningPlanId
     * @param excludeSchoolIds
     */
    public void deleteByPlanIdAndExcludeSchoolIds(Integer screeningPlanId, List<Integer> excludeSchoolIds) {
        Assert.notNull(screeningPlanId, "筛查计划ID不能为空");
        QueryWrapper<ScreeningPlanSchoolStudent> query = new QueryWrapper<ScreeningPlanSchoolStudent>().eq("screening_plan_id", screeningPlanId);
        if (CollectionUtils.hasLength(excludeSchoolIds)) {
            query.notIn("school_id", excludeSchoolIds);
        }
        baseMapper.delete(query);
    }

    /**
     * 根据计划ID获取所有筛查学生
     *
     * @param screeningPlanId
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByScreeningPlanId(Integer screeningPlanId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningPlanSchoolStudent>().eq("screening_plan_id", screeningPlanId));
    }

    /**
     * 根据计划ID获取学校ID的学生数Map
     *
     * @param screeningPlanId
     * @return
     */
    public Map<Integer, Long> getSchoolStudentCountByScreeningPlanId(Integer screeningPlanId) {
        return getByScreeningPlanId(screeningPlanId).stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId, Collectors.counting()));
    }

    /**
     * 获取计划中的学校年级情况
     *
     * @param screeningPlanId
     * @param schoolId
     * @return
     */
    public List<SchoolGradeVo> getSchoolGradeVoByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        List<GradeClassesDTO> gradeClasses = baseMapper.selectSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId);
        List<SchoolGradeVo> schoolGradeVos = new ArrayList<>();
        Map<Integer, List<GradeClassesDTO>> graderIdClasses = gradeClasses.stream().collect(Collectors.groupingBy(GradeClassesDTO::getGradeId));
        graderIdClasses.keySet().forEach(gradeId -> {
            SchoolGradeVo vo = new SchoolGradeVo();
            List<GradeClassesDTO> gradeClassesDTOS = graderIdClasses.get(gradeId);
            vo.setId(gradeId).setName(gradeClassesDTOS.get(0).getGradeName());
            vo.setClasses(gradeClassesDTOS.stream().map(dto -> {
                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setId(dto.getClassId()).setName(dto.getClassName());
                return schoolClass;
            }).collect(Collectors.toList()));
            schoolGradeVos.add(vo);
        });
        return schoolGradeVos;
    }

    /**
     * 分页获取筛查计划的学校学生数据
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<StudentDTO> getPage(StudentQuery query, PageRequest pageRequest) {
        Assert.notNull(query.getScreeningPlanId(), "筛查计划ID不能为空");
        Assert.notNull(query.getSchoolId(), "筛查学校ID不能为空");
        Page<StudentDTO> page = (Page<StudentDTO>) pageRequest.toPage();
        IPage<StudentDTO> studentDTOIPage = baseMapper.selectPageByQuery(page, query);
        studentDTOIPage.getRecords().forEach(studentDTO -> studentDTO.setNationDesc(NationEnum.getName(studentDTO.getNation())).setAddress(districtService.getAddressDetails(studentDTO.getProvinceCode(), studentDTO.getCityCode(), studentDTO.getAreaCode(), studentDTO.getTownCode(), studentDTO.getAddress())));
        return studentDTOIPage;
    }

    /**
     * 根据身份证号获取筛查学生
     *
     * @param screeningPlanId
     * @param schoolId
     * @param idCardList
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByIdCards(Integer screeningPlanId, Integer schoolId, List<String> idCardList) {
        return Lists.partition(idCardList, 50).stream().map(list -> baseMapper.selectByIdCards(screeningPlanId, schoolId, list)).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * 处理上传的筛查学生数据
     *
     * @param listMap
     */
    public void insertByUpload(Integer userId, List<Map<Integer, String>> listMap, Integer screeningPlanId, Integer schoolId) {
        School school = schoolService.getById(schoolId);
        checkSchoolExistAndListMapNeededExist(school, listMap);
        // 获取所有身份证号
        Set<String> idCardSet = new HashSet<>();
        Set<String> gradeNameSet = new HashSet<>();
        Set<String> gradeClassNameSet = new HashSet<>();
        Map<String, List<Long>> districtNameCodeMap = new HashMap<>(16);
        listMap.forEach(item -> {
            String gradeName = item.getOrDefault(5, null);
            String className = item.getOrDefault(6, null);
            String idCard = item.getOrDefault(8, null);
            String provinceName = item.getOrDefault(10, null);
            String cityName = item.getOrDefault(11, null);
            String areaName = item.getOrDefault(12, null);
            String townName = item.getOrDefault(13, null);
            idCardSet.add(idCard);
            gradeNameSet.add(gradeName);
            gradeClassNameSet.add(String.format("%s-%s", gradeName, className));
            if (StringUtils.allHasLength(provinceName, cityName, areaName, townName)) {
                districtNameCodeMap.put(String.format("%s-%s-%s-%s", provinceName, cityName, areaName, townName), districtService.getCodeByName(provinceName, cityName, areaName, townName));
            }
        });
        Map<String, Integer> gradeNameIdMap = schoolGradeService.getBySchoolId(schoolId).stream().collect(Collectors.toMap(SchoolGrade::getName, SchoolGrade::getId));
        Map<String, Integer> gradeClassNameClassIdMap = schoolClassService.getVoBySchoolId(schoolId).stream().collect(Collectors.toMap(schoolClass -> String.format("%s-%s", schoolClass.getGradeName(), schoolClass.getName()), SchoolClass::getId));
        checkExcelDataLegal(idCardSet, gradeNameSet, gradeClassNameSet, gradeNameIdMap, gradeClassNameClassIdMap);
        // 根据身份证号分批获取已有的学生
        Map<String, Student> idCardExistStudents = studentService.getByIdCards(new ArrayList<>(idCardSet)).stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        // 根据身份证号分批获取已有的筛查学生数据
        Map<String, ScreeningPlanSchoolStudent> idCardExistScreeningStudents = getByIdCards(screeningPlanId, schoolId, new ArrayList<>(idCardSet)).stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getIdCard, Function.identity()));
        List<Student> excelStudents = getStudentListFromExcelItem(listMap, gradeNameIdMap, gradeClassNameClassIdMap, districtNameCodeMap);
        Map<String, Student> excelIdCardStudentMap = excelStudents.stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        // 1. 筛选出需新增的学生并新增
        addStudents(userId, idCardExistStudents, excelIdCardStudentMap);
        // 2. 已有的要判断是否需更新
        updateStudents(idCardExistStudents, excelIdCardStudentMap);
        // 3. 处理筛查学生
        addOrUpdateScreeningPlanStudents(screeningPlanId, schoolId, school, idCardExistStudents, idCardExistScreeningStudents, excelStudents);
    }

    /**
     * 校验学校是否存在，表格中必填项是否都有
     * @param school
     * @param listMap
     */
    private void checkSchoolExistAndListMapNeededExist(School school, List<Map<Integer, String>> listMap) {
        if (Objects.isNull(school)) {
            throw new BusinessException("不存在该学校");
        }
        if (listMap.stream().anyMatch(map -> ObjectsUtil.hasNull(map.getOrDefault(0, null), map.getOrDefault(1, null), map.getOrDefault(2, null), map.getOrDefault(4, null),map.getOrDefault(5, null), map.getOrDefault(6, null), map.getOrDefault(7, null), map.getOrDefault(8, null)))) {
            throw new BusinessException("存在必填项无填写");
        }
    }

    /**
     * 处理筛查学生
     * @param screeningPlanId
     * @param schoolId
     * @param school
     * @param idCardExistStudents
     * @param idCardExistScreeningStudents
     * @param excelStudents
     */
    private void addOrUpdateScreeningPlanStudents(Integer screeningPlanId, Integer schoolId, School school, Map<String, Student> idCardExistStudents, Map<String, ScreeningPlanSchoolStudent> idCardExistScreeningStudents, List<Student> excelStudents) {
        List<ScreeningPlanSchoolStudent> addOrUpdatePlanStudents = excelStudents.stream().map(student -> {
            ScreeningPlanSchoolStudent existPlanStudent = idCardExistScreeningStudents.getOrDefault(student.getIdCard(), null);
            Student dbStudent = idCardExistStudents.get(student.getIdCard());
            if (Objects.isNull(existPlanStudent)) {
                existPlanStudent = new ScreeningPlanSchoolStudent();
                existPlanStudent.setIdCard(student.getIdCard()).setScreeningPlanId(screeningPlanId).setSchoolId(schoolId).setSchoolName(school.getName()).setStudentId(dbStudent.getId());
            }
            existPlanStudent.setStudentName(student.getName())
                    .setGradeId(student.getGradeId()).setClassId(student.getClassId())
                    .setStudentAge(AgeUtil.countAge(student.getBirthday()))
                    .setStudentSituation(SerializationUtil.serializeWithoutException(dbStudent))
                    .setStudentNo(dbStudent.getSno());
            return existPlanStudent;
        }).collect(Collectors.toList());
        saveOrUpdateBatch(addOrUpdatePlanStudents);
    }

    /**
     * 更新学生数据
     * @param idCardExistStudents
     * @param excelIdCardStudentMap
     */
    private void updateStudents(Map<String, Student> idCardExistStudents, Map<String, Student> excelIdCardStudentMap) {
        List<String> needCheckUpdateStudentIdCards = CompareUtil.getRetain(idCardExistStudents.keySet(), excelIdCardStudentMap.keySet());
        List<Student> updateStudents = new ArrayList<>();
        needCheckUpdateStudentIdCards.forEach(idCard -> {
            Student student = idCardExistStudents.get(idCard);
            Student excelStudent = excelIdCardStudentMap.get(idCard);
            if (student.checkNeedUpdate(excelStudent)) {
                Student updateStudent = new Student();
                BeanUtils.copyProperties(student, updateStudent);
                updateStudent.setName(excelStudent.getName())
                        .setSchoolNo(excelStudent.getSchoolNo())
                        .setGender(excelStudent.getGender())
                        .setBirthday(excelStudent.getBirthday())
                        .setNation(ObjectsUtil.getDefaultIfNull(excelStudent.getNation(), student.getNation()))
                        .setGradeId(excelStudent.getGradeId())
                        .setClassId(excelStudent.getClassId())
                        .setSno(excelStudent.getSno())
                        .setProvinceCode(ObjectsUtil.getDefaultIfNull(excelStudent.getProvinceCode(), student.getProvinceCode()))
                        .setCityCode(ObjectsUtil.getDefaultIfNull(excelStudent.getCityCode(), student.getCityCode()))
                        .setAreaCode(ObjectsUtil.getDefaultIfNull(excelStudent.getAreaCode(), student.getAreaCode()))
                        .setTownCode(ObjectsUtil.getDefaultIfNull(excelStudent.getTownCode(), student.getTownCode()))
                        .setAddress(StringUtils.getDefaultIfBlank(excelStudent.getAddress(), student.getAddress()))
                        .setParentPhone(StringUtils.getDefaultIfBlank(excelStudent.getParentPhone(), student.getParentPhone()));
                updateStudents.add(updateStudent);
            }
        });
        studentService.updateBatchById(updateStudents);
    }

    /**
     * 新增学生数据
     * @param userId
     * @param idCardExistStudents
     * @param excelIdCardStudentMap
     */
    private void addStudents(Integer userId, Map<String, Student> idCardExistStudents, Map<String, Student> excelIdCardStudentMap) {
        List<String> needAddedIdCards = CompareUtil.getAdded(new ArrayList<>(idCardExistStudents.keySet()), new ArrayList<>(excelIdCardStudentMap.keySet()));
        if (CollectionUtils.hasLength(needAddedIdCards)) {
            List<Student> addedStudent = needAddedIdCards.stream().map(idCard -> excelIdCardStudentMap.get(idCard)).collect(Collectors.toList());
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
     * @param gradeNameSet
     * @param gradeClassNameSet
     * @param gradeNameIdMap
     * @param gradeClassNameClassIdMap
     */
    private void checkExcelDataLegal(Set<String> idCardList, Set<String> gradeNameSet, Set<String> gradeClassNameSet, Map<String, Integer> gradeNameIdMap, Map<String, Integer> gradeClassNameClassIdMap) {
        // 身份证号是否符合规则
        if (!idCardList.stream().allMatch(RegularUtils::isIdCard)) {
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
    }

    /**
     * 根据excel数据生成学生数据列表
     *
     * @param listMap
     * @param gradeNameIdMap
     * @param gradeClassNameClassIdMap
     * @param districtNameCodeMap
     * @return
     */
    private List<Student> getStudentListFromExcelItem(List<Map<Integer, String>> listMap, Map<String, Integer> gradeNameIdMap, Map<String, Integer> gradeClassNameClassIdMap, Map<String, List<Long>> districtNameCodeMap) {
        // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
        List<Student> excelStudents = listMap.stream().map(item -> {
            try {
                return generateStudentByExcelItem(item, gradeNameIdMap, gradeClassNameClassIdMap, districtNameCodeMap);
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
     * @param gradeClassNameClassIdMap @return
     */
    private Student generateStudentByExcelItem(Map<Integer, String> item, Map<String, Integer> gradeNameIdMap, Map<String, Integer> gradeClassNameClassIdMap, Map<String, List<Long>> districtNameCodeMap) throws ParseException {
        try {
            // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
            Student student = new Student();
            student.setName(StringUtils.getDefaultIfBlank(item.get(0), null))
                    .setGender(StringUtils.isBlank(item.get(1)) ? null : GenderEnum.getType(item.get(1)))
                    .setBirthday(StringUtils.isBlank(item.get(2)) ? null : DateFormatUtil.parseDate(item.get(2), DateFormatUtil.FORMAT_ONLY_DATE2))
                    .setNation(StringUtils.isBlank(item.get(3)) ? null : NationEnum.getCode(item.get(3)))
                    .setSchoolNo(StringUtils.getDefaultIfBlank(item.get(4), null))
                    .setGradeId(gradeNameIdMap.get(item.get(5)))
                    .setClassId(gradeClassNameClassIdMap.get(String.format("%s-%s", item.get(5), item.get(6))))
                    .setSno(StringUtils.getDefaultIfBlank(item.get(7), null))
                    .setIdCard(StringUtils.getDefaultIfBlank(item.get(8), null))
                    .setParentPhone(StringUtils.getDefaultIfBlank(item.get(9), null))
                    .setAddress(StringUtils.getDefaultIfBlank(item.get(14), null));
            String provinceName = item.getOrDefault(10, null);
            String cityName = item.getOrDefault(11, null);
            String areaName = item.getOrDefault(12, null);
            String townName = item.getOrDefault(13, null);
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

    /**
     * 根据年级班级ID获取筛查学生
     * @param gradeId
     * @param classId
     * @return
     */
    public List<StudentDTO> getByGradeAndClass(Integer gradeId, Integer classId) {
        return baseMapper.selectByGradeAndClass(gradeId, classId);
    }
}
