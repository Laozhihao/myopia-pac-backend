package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.business.common.exceptions.ManagementUncheckedException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.*;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.GradeCodeEnum;
import com.wupol.myopia.business.management.domain.dto.ScreeningResultSearchDTO;
import com.wupol.myopia.business.management.domain.dto.StudentScreeningInfoWithResultDTO;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.management.constant.GenderEnum;
import com.wupol.myopia.business.management.constant.ImportExcelEnum;
import com.wupol.myopia.business.management.constant.NationEnum;
import com.wupol.myopia.business.management.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanSchoolStudentMapper;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.domain.vo.SchoolGradeVo;
import com.wupol.myopia.business.management.domain.vo.StudentVo;
import com.wupol.myopia.business.management.util.AgeUtil;
import com.wupol.myopia.business.management.util.SerializationUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Autowired
    private ScreeningPlanService screeningPlanService;

    /**
     * 根据学生id获取筛查计划学校学生
     *
     * @param studentId 学生ID
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getByStudentId(Integer studentId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningPlanSchoolStudent>().eq("student_id", studentId));
    }

/*    public ScreeningPlanSchoolStudent getByScreeningPlanSchoolStudentId(Integer studentId) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent();
        screeningPlanSchoolStudent.set(studentId);
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.setEntity(screeningPlanSchoolStudent);
        return baseMapper.selectOne(queryWrapper);
    }*/

    /**
     * 批量查找数据
     *
     * @param screeningResultSearchDTO
     * @return
     */
    public List<StudentScreeningInfoWithResultDTO> getStudentInfoWithResult(ScreeningResultSearchDTO screeningResultSearchDTO) {
        List<StudentScreeningInfoWithResultDTO> visionScreeningResults = baseMapper.selectStudentInfoWithResult(screeningResultSearchDTO);
        return visionScreeningResults;
    }

    /**
     * @param schoolName
     * @param deptId
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getSchoolByOrgIdAndSchoolName(String schoolName, Integer deptId) {
        if (deptId == null) {
            throw new ManagementUncheckedException("deptId 不能为空");
        }
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(deptId);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new ArrayList<>();
        }
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningOrgId, deptId).like(ScreeningPlanSchoolStudent::getSchoolName, schoolName).in(ScreeningPlanSchoolStudent::getScreeningPlanId,currentPlanIds);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = baseMapper.selectList(queryWrapper);
        return screeningPlanSchoolStudents;
    }

    public List<ScreeningPlanSchoolStudent> getClassNameBySchoolNameAndGradeName(String schoolName, String gradeName, Integer deptId) {
        if (deptId == null) {
            throw new ManagementUncheckedException("deptId 不能为空");
        }
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(deptId);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningOrgId, deptId).in(ScreeningPlanSchoolStudent::getScreeningPlanId,currentPlanIds).eq(ScreeningPlanSchoolStudent::getGradeName, gradeName).eq(ScreeningPlanSchoolStudent::getSchoolName, schoolName);
        List<ScreeningPlanSchoolStudent> screeningPlanSchools = baseMapper.selectList(queryWrapper);
        return screeningPlanSchools;
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
     * 根据计划ID和学校ID获取所有筛查学生
     *
     * @param screeningPlanId
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByScreeningPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningPlanSchoolStudent>().eq("screening_plan_id", screeningPlanId).eq("school_id", schoolId));
    }

    /**
     * 根据计划ID获取所有筛查学生数量
     *
     * @param screeningPlanId
     * @return
     */
    public Integer getCountByScreeningPlanId(Integer screeningPlanId) {
        return baseMapper.selectCount(new QueryWrapper<ScreeningPlanSchoolStudent>().eq("screening_plan_id", screeningPlanId));
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
     * 获取学校筛查学生数
     * @param srcScreeningNoticeId 通知ID
     * @param schoolId 学校ID
     * @return
     */
    public Integer countPlanSchoolStudent(int srcScreeningNoticeId, int schoolId) {
        return baseMapper.selectCount(new QueryWrapper<ScreeningPlanSchoolStudent>()
                                              .eq("school_id", schoolId)
                                              .eq("src_screening_notice_id", srcScreeningNoticeId));
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
        if (StringUtils.hasLength(query.getGradeIds())) {
            query.setGradeList(Stream.of(StringUtils.commaDelimitedListToStringArray(query.getGradeIds())).map(Integer::parseInt).collect(Collectors.toList()));
        }
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
    public void insertByUpload(Integer userId, List<Map<Integer, String>> listMap, ScreeningPlan screeningPlan, Integer schoolId) {
        School school = schoolService.getById(schoolId);
        // 校验学校是否存在，表格中必填项是否都有
        List<String> snoList = checkSchoolAndNeededExistWithReturnSnoList(school, listMap);
        // 获取所有身份证号
        Set<String> idCardSet = new HashSet<>();
        Set<String> gradeNameSet = new HashSet<>();
        Set<String> gradeClassNameSet = new HashSet<>();
        Map<String, List<Long>> districtNameCodeMap = new HashMap<>(16);
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
        Map<Boolean, List<ScreeningPlanSchoolStudent>> alreadyExistOrNotStudents = getByScreeningPlanIdAndSchoolId(screeningPlan.getId(), schoolId).stream().collect(Collectors.groupingBy(planStudent -> idCardSet.contains(planStudent.getIdCard())));
        Map<String, Integer> gradeNameIdMap = schoolGradeService.getBySchoolId(schoolId).stream().collect(Collectors.toMap(SchoolGrade::getName, SchoolGrade::getId));
        Map<String, Integer> gradeClassNameClassIdMap = schoolClassService.getVoBySchoolId(schoolId).stream().collect(Collectors.toMap(schoolClass -> String.format("%s-%s", schoolClass.getGradeName(), schoolClass.getName()), SchoolClass::getId));
        // 校验数据是否合法
        checkExcelDataLegal(idCardSet, snoList, gradeNameSet, gradeClassNameSet, gradeNameIdMap, gradeClassNameClassIdMap, alreadyExistOrNotStudents.get(false));
        // 根据身份证号分批获取已有的学生
        Map<String, Student> idCardExistStudents = studentService.getByIdCards(new ArrayList<>(idCardSet)).stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        // 获取已有的筛查学生数据
        Map<String, ScreeningPlanSchoolStudent> idCardExistScreeningStudents = CollectionUtils.isEmpty(alreadyExistOrNotStudents.get(true)) ? Collections.emptyMap() : alreadyExistOrNotStudents.get(true).stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getIdCard, Function.identity()));
        List<StudentVo> excelStudents = getStudentListFromExcelItem(listMap, gradeNameIdMap, gradeClassNameClassIdMap, districtNameCodeMap, school.getSchoolNo());
        Map<String, StudentVo> excelIdCardStudentMap = excelStudents.stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        // 1. 筛选出需新增的学生并新增
        addStudents(userId, idCardExistStudents, excelIdCardStudentMap);
        // 2. 已有的要判断是否需更新
        updateStudents(idCardExistStudents, excelIdCardStudentMap);
        // 3. 处理筛查学生
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
     * @param screeningPlan
     * @param schoolId
     * @param school
     * @param idCardExistStudents
     * @param idCardExistScreeningStudents
     * @param excelStudents
     */
    private void addOrUpdateScreeningPlanStudents(ScreeningPlan screeningPlan, Integer schoolId, School school, Map<String, Student> idCardExistStudents, Map<String, ScreeningPlanSchoolStudent> idCardExistScreeningStudents, List<StudentVo> excelStudents) {
        List<ScreeningPlanSchoolStudent> addOrUpdatePlanStudents = excelStudents.stream().map(student -> {
            ScreeningPlanSchoolStudent existPlanStudent = idCardExistScreeningStudents.getOrDefault(student.getIdCard(), null);
            Student dbStudent = idCardExistStudents.get(student.getIdCard());
            if (Objects.isNull(existPlanStudent)) {
                existPlanStudent = new ScreeningPlanSchoolStudent();
                existPlanStudent.setIdCard(student.getIdCard()).setSrcScreeningNoticeId(screeningPlan.getSrcScreeningNoticeId()).setScreeningTaskId(screeningPlan.getScreeningTaskId()).setScreeningPlanId(screeningPlan.getId())
                        .setScreeningOrgId(screeningPlan.getScreeningOrgId()).setDistrictId(screeningPlan.getDistrictId()).setSchoolId(schoolId).setSchoolName(school.getName()).setStudentId(dbStudent.getId());
            }
            existPlanStudent.setStudentName(student.getName())
                    .setGradeId(student.getGradeId())
                    .setGradeName(student.getGradeName())
                    .setGradeType(GradeCodeEnum.getByName(student.getGradeName()).getType())
                    .setClassId(student.getClassId())
                    .setClassName(student.getClassName())
                    .setBirthday(student.getBirthday())
                    .setGender(student.getGender())
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
    private void updateStudents(Map<String, Student> idCardExistStudents, Map<String, StudentVo> excelIdCardStudentMap) {
        List<String> needCheckUpdateStudentIdCards = CompareUtil.getRetain(idCardExistStudents.keySet(), excelIdCardStudentMap.keySet());
        List<Student> updateStudents = new ArrayList<>();
        needCheckUpdateStudentIdCards.forEach(idCard -> {
            Student student = idCardExistStudents.get(idCard);
            StudentVo excelStudent = excelIdCardStudentMap.get(idCard);
            if (student.checkNeedUpdate(excelStudent)) {
                Student updateStudent = new Student();
                BeanUtils.copyProperties(student, updateStudent);
                updateStudent.setName(excelStudent.getName())
                        .setSchoolNo(excelStudent.getSchoolNo())
                        .setGender(excelStudent.getGender())
                        .setBirthday(excelStudent.getBirthday())
                        .setNation(ObjectsUtil.getDefaultIfNull(excelStudent.getNation(), student.getNation()))
                        .setGradeId(excelStudent.getGradeId())
                        .setGradeType(GradeCodeEnum.getByName(excelStudent.getGradeName()).getType())
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
    private void addStudents(Integer userId, Map<String, Student> idCardExistStudents, Map<String, StudentVo> excelIdCardStudentMap) {
        List<String> needAddedIdCards = CompareUtil.getAdded(new ArrayList<>(idCardExistStudents.keySet()), new ArrayList<>(excelIdCardStudentMap.keySet()));
        if (CollectionUtils.hasLength(needAddedIdCards)) {
            List<Student> addedStudent = needAddedIdCards.stream().map(idCard -> {
                Student s = new Student();
                StudentVo excelStudent = excelIdCardStudentMap.get(idCard);
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
    private List<StudentVo> getStudentListFromExcelItem(List<Map<Integer, String>> listMap, Map<String, Integer> gradeNameIdMap, Map<String, Integer> gradeClassNameClassIdMap, Map<String, List<Long>> districtNameCodeMap, String schoolNo) {
        // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
        List<StudentVo> excelStudents = listMap.stream().map(item -> {
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
    private StudentVo generateStudentByExcelItem(Map<Integer, String> item, Map<String, Integer> gradeNameIdMap, Map<String, Integer> gradeClassNameClassIdMap, Map<String, List<Long>> districtNameCodeMap, String schoolNo) {
        try {
            // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
            StudentVo student = new StudentVo();
            student.setGradeName(item.get(ImportExcelEnum.GRADE.getIndex())).setClassName(item.get(ImportExcelEnum.CLASS.getIndex()));
            student.setName(StringUtils.getDefaultIfBlank(item.get(ImportExcelEnum.NAME.getIndex()), null))
                    .setGender(StringUtils.isBlank(item.get(ImportExcelEnum.GENDER.getIndex())) ? null : GenderEnum.getType(item.get(ImportExcelEnum.GENDER.getIndex())))
                    .setBirthday(StringUtils.isBlank(item.get(ImportExcelEnum.BIRTHDAY.getIndex())) ? null : DateFormatUtil.parseDate(item.get(ImportExcelEnum.BIRTHDAY.getIndex()), DateFormatUtil.FORMAT_ONLY_DATE2))
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

    /**
     * 根据年级班级ID获取筛查学生
     *
     * @param gradeId
     * @param classId
     * @return
     */
    public List<StudentDTO> getByGradeAndClass(Integer screeningPlanId, Integer gradeId, Integer classId) {
        return baseMapper.selectByGradeAndClass(screeningPlanId, gradeId, classId);
    }

    /**
     * @param screeningPlanSchoolStudent
     */
    public List<ScreeningPlanSchoolStudent> listByEntityDescByCreateTime(ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        //获取当前计划
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(screeningPlanSchoolStudent.getScreeningOrgId());
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new ArrayList<>();
        }
        String studentName = screeningPlanSchoolStudent.getStudentName();
        screeningPlanSchoolStudent.setStudentName(null);
        queryWrapper.setEntity(screeningPlanSchoolStudent).in(ScreeningPlanSchoolStudent::getScreeningPlanId,currentPlanIds);
        if (StringUtils.isNotBlank(studentName)) {
            queryWrapper.like(ScreeningPlanSchoolStudent::getStudentName, studentName);
        }
        return baseMapper.selectList(queryWrapper);
    }
}
