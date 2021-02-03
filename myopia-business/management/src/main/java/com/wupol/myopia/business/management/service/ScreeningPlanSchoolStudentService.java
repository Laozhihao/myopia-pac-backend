package com.wupol.myopia.business.management.service;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.management.constant.NationEnum;
import com.wupol.myopia.business.management.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanSchoolStudentMapper;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.domain.vo.SchoolGradeVo;
import com.wupol.myopia.business.management.util.SerializationUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.SerializationUtils;

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
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;

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
        if (!CollectionUtils.isEmpty(excludeSchoolIds)) {
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
        studentDTOIPage.getRecords().forEach(studentDTO -> studentDTO.setNationDesc(NationEnum.getName(studentDTO.getNation())));
        return studentDTOIPage;
    }

    /**
     * 根据身份证号获取筛查学生
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
        // 获取所有身份证号
        List<String> idCardList = listMap.stream().map(item -> item.getOrDefault(8, null)).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        // 身份证号是否符合规则
        if (!idCardList.stream().allMatch(RegularUtils::isIdCard)) {
            throw new BusinessException("存在不正确的身份证号");
        }
        // 根据学校ID获取年级班级信息
        Map<String, Integer> gradeNameIdMap = schoolGradeService.getBySchoolId(schoolId).stream().collect(Collectors.toMap(SchoolGrade::getName, SchoolGrade::getId));
        // 获取所有年级名
        List<String> gradeNameList = listMap.stream().map(item -> item.getOrDefault(5, null)).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        // 年级名与班级名是否都存在
        if (gradeNameList.stream().anyMatch(gradeName -> !gradeNameIdMap.keySet().contains(gradeName))) {
            throw new BusinessException("存在不正确的年级名称");
        }
        Map<String, Integer> classNameIdMap = schoolClassService.getBySchoolId(schoolId).stream().collect(Collectors.toMap(SchoolClass::getName, SchoolClass::getId));
        List<String> classNameList = listMap.stream().map(item -> item.getOrDefault(6, null)).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (classNameList.stream().anyMatch(gradeName -> !classNameIdMap.keySet().contains(gradeName))) {
            throw new BusinessException("存在不正确的班级名称");
        }
        // 根据身份证号分批获取已有的学生
        Map<String, Student> idCardExistStudents = studentService.getByIdCards(idCardList).stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        // 根据身份证号分批获取已有的筛查学生数据
        Map<String, ScreeningPlanSchoolStudent> idCardExistScreeningStudents = getByIdCards(screeningPlanId, schoolId, idCardList).stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getIdCard, Function.identity()));
        // excel格式：序号、姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、年级、班级、学号、身份证号、手机号码、居住地址
        List<Student> excelStudents = listMap.stream().map(item -> {
            try {
                return generateStudentByExcelItem(item, gradeNameIdMap, classNameIdMap);
            } catch (Exception e) {
                log.error("导入筛查学生数据异常", e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if (excelStudents.size() != listMap.size()) {
            throw new BusinessException("学生数据有误，请检查");
        }
        // 1. 筛选出需新增的学生并新增得ID
        // 2. 其它学生判断是否需要更新
        // 2.1 不需要更新，插入ID，并新增ScreeningPlanSchoolStudent
        // 2.1 需要更新，加入到更新学生数据。插入ID，并新增ScreeningPlanSchoolStudent
//        List<Student> needAddOrUpdateStudents = new ArrayList<>();
//        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = excelStudents.stream()
//                .map(student -> {
//                    ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent();
//                    Student existStudent = idCardExistStudents.getOrDefault(student.getIdCard(), null);
//                    if (true) {
//                        // TODO
//                        // 需要更新的学生
//                        // 先备份原先学生数据
//                        if (Objects.nonNull(existStudent)) {
//                            screeningPlanSchoolStudent.setStudentSituation(SerializationUtil.serializeWithoutException(existStudent));
//                            student.setId(existStudent.getId());
//                        }
//                        needAddOrUpdateStudents.add(student);
//                    }
//                    BeanUtils.copyProperties(student, screeningPlanSchoolStudent);
//                    return screeningPlanSchoolStudent.setScreeningPlanId(screeningPlanId).setSchoolId(schoolId);
//                }).collect(Collectors.toList());
//        // 批量新增, 并设置返回的userId
//        studentService.saveOrUpdateBatch(needAddOrUpdateStudents);
//        //TODO 已有处理
//        saveBatch(screeningPlanSchoolStudents);
    }

    /**
     * 根据上传筛查学生的表格项生成Student
     * @param item
     * @param gradeNameIdMap
     *@param classNameIdMap @return
     */
    private Student generateStudentByExcelItem(Map<Integer, String> item, Map<String, Integer> gradeNameIdMap, Map<String, Integer> classNameIdMap) throws ParseException {
        // excel格式：序号、姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、年级、班级、学号、身份证号、手机号码、居住地址
        Student student = new Student();
        student.setName(StringUtils.getDefaultIfBlank(item.get(1), null))
                .setGender(StringUtils.isBlank(item.get(2)) ? null : Integer.parseInt(item.get(2)))
                .setBirthday(StringUtils.isBlank(item.get(3)) ? null : DateFormatUtil.parseDate(item.get(3), DateFormatUtil.FORMAT_ONLY_DATE2))
                .setNation(StringUtils.isBlank(item.get(4)) ? null : Integer.parseInt(item.get(4)))
                .setGradeId(gradeNameIdMap.getOrDefault(item.get(5), null))
                .setClassId(gradeNameIdMap.getOrDefault(item.get(6), null))
                .setSchoolNo(StringUtils.getDefaultIfBlank(item.get(7), null))
                .setIdCard(StringUtils.getDefaultIfBlank(item.get(8), null))
                .setParentPhone(StringUtils.getDefaultIfBlank(item.get(9), null))
                .setAddress(StringUtils.getDefaultIfBlank(item.get(10), null));
        return student;
    }
}
