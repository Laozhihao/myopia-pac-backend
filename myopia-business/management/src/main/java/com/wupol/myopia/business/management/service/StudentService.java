package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.CacheKey;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.constant.GradeCodeEnum;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.dto.StudentScreeningResultResponse;
import com.wupol.myopia.business.management.domain.mapper.StudentMapper;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.domain.vo.StudentCountVO;
import com.wupol.myopia.business.management.domain.vo.StudentScreeningCountVO;
import com.wupol.myopia.business.management.util.TwoTuple;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
@Log4j2
public class StudentService extends BaseService<StudentMapper, Student> {

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private ScreeningResultService screeningResultService;

    @Resource
    private SchoolService schoolService;

    /**
     * 通过年级id查找学生
     *
     * @param gradeId 年级Id
     * @return 学生列表
     */
    public List<Student> getStudentsByGradeId(Integer gradeId) {
        QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
        equalsQueryAppend(studentQueryWrapper, "grade_id", gradeId);
        notEqualsQueryAppend(studentQueryWrapper, "status", CommonConst.STATUS_IS_DELETED);
        return baseMapper.selectList(studentQueryWrapper);
    }

    /**
     * 通过班级id查找学生
     *
     * @param classId 班级Id
     * @return 学生列表
     */
    public List<Student> getStudentsByClassId(Integer classId) {
        QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
        equalsQueryAppend(studentQueryWrapper, "class_id", classId);
        notEqualsQueryAppend(studentQueryWrapper, "status", CommonConst.STATUS_IS_DELETED);
        return baseMapper.selectList(studentQueryWrapper);
    }

    /**
     * 新增学生
     *
     * @param student 学生实体类
     * @return 新增数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveStudent(Student student) {

        Integer createUserId = student.getCreateUserId();
        String idCard = student.getIdCard();

        // 设置学龄
        if (null != student.getGradeId()) {
            SchoolGrade grade = schoolGradeService.getById(student.getGradeId());
            student.setGradeType(GradeCodeEnum.getByCode(grade.getGradeCode()).getType());
        }

        // 检查学生身份证是否重复
        if (checkIdCard(student.getIdCard(), null)) {
            throw new BusinessException("学生身份证重复");
        }

        RLock rLock = redissonClient.getLock(String.format(CacheKey.LOCK_STUDENT_REDIS, idCard));
        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {
                return baseMapper.insert(student);
            }
        } catch (InterruptedException e) {
            log.error("用户:{}创建学生获取锁异常,e:{}", createUserId, e);
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            if (rLock.isLocked()) {
                rLock.unlock();
            }
        }
        log.warn("用户id:{}新增学生获取不到锁，新增学生身份证:{}", createUserId, idCard);
        throw new BusinessException("请重试");
    }

    /**
     * 更新学生
     *
     * @param student 学生实体类
     * @return 学生实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public StudentDTO updateStudent(Student student) {

        // 设置学龄
        if (null != student.getGradeId()) {
            SchoolGrade grade = schoolGradeService.getById(student.getGradeId());
            student.setGradeType(GradeCodeEnum.getByCode(grade.getGradeCode()).getType());
        }

        // 检查学生身份证是否重复
        if (checkIdCard(student.getIdCard(), student.getId())) {
            throw new BusinessException("学生身份证重复");
        }

        if (null == student.getTownCode()){
            student.setTownCode(0L);
        }

        // 更新学生
        baseMapper.updateById(student);
        Student resultStudent = baseMapper.selectById(student.getId());
        StudentDTO studentDTO = new StudentDTO();
        BeanUtils.copyProperties(resultStudent, studentDTO);
        if (StringUtils.isNotBlank(studentDTO.getSchoolNo())) {
            School school = schoolService.getBySchoolNo(studentDTO.getSchoolNo());
            studentDTO.setSchoolName(school.getName());
            studentDTO.setSchoolId(school.getId());

            // 查询年级和班级
            SchoolGrade schoolGrade = schoolGradeService.getById(resultStudent.getGradeId());
            SchoolClass schoolClass = schoolClassService.getById(resultStudent.getClassId());
            studentDTO.setGradeName(schoolGrade.getName()).setClassName(schoolClass.getName());
        }
        studentDTO.setScreeningCount(student.getScreeningCount())
                .setQuestionnaireCount(student.getQuestionnaireCount())
                .setSeeDoctorCount(student.getSeeDoctorCount());
        return studentDTO;
    }

    /**
     * 删除学生
     *
     * @param id 学生id
     * @return 删除个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedStudent(Integer id) {
        Student student = new Student();
        student.setId(id);
        student.setStatus(CommonConst.STATUS_IS_DELETED);
        return baseMapper.updateById(student);
    }

    /**
     * 获取学生列表
     *
     * @param pageRequest  分页
     * @param studentQuery 请求体
     * @return IPage<Student> {@link IPage}
     */
    public IPage<StudentDTO> getStudentLists(PageRequest pageRequest, StudentQuery studentQuery) {

        TwoTuple<List<Integer>, List<Integer>> conditionalFilter = conditionalFilter(
                studentQuery.getGradeIds(), studentQuery.getVisionLabels());

        IPage<StudentDTO> pageStudents = baseMapper.getStudentListByCondition(pageRequest.toPage(),
                studentQuery.getSno(), studentQuery.getIdCard(), studentQuery.getName(),
                studentQuery.getParentPhone(), studentQuery.getGender(), conditionalFilter.getFirst(),
                conditionalFilter.getSecond(), studentQuery.getStartScreeningTime(), studentQuery.getEndScreeningTime(),
                studentQuery.getSchoolName());
        List<StudentDTO> students = pageStudents.getRecords();

        // 为空直接放回
        if (CollectionUtils.isEmpty(students)) {
            return pageStudents;
        }

        // 获取年级信息
        Map<Integer, SchoolGrade> gradeMaps = schoolGradeService.getGradeMapByIds(students
                .stream().map(Student::getGradeId).collect(Collectors.toList()));

        // 获取班级信息
        Map<Integer, SchoolClass> classMaps = schoolClassService.getClassMapByIds(students
                .stream().map(Student::getClassId).collect(Collectors.toList()));

        // 学校信息
        Map<String, School> schoolMaps = schoolService.getNameBySchoolNos(students.stream().map(Student::getSchoolNo).collect(Collectors.toList()));

        // 筛查次数
        List<StudentScreeningCountVO> studentScreeningCountVOS = screeningResultService.countScreeningTime();
        Map<Integer, Integer> countMaps = studentScreeningCountVOS.stream().collect(Collectors
                .toMap(StudentScreeningCountVO::getStudentId,
                        StudentScreeningCountVO::getCount));

        // 封装DTO
        students.forEach(s -> {

            // 学校编码不为空才显示班级和年级信息
            if (StringUtils.isNotBlank(s.getSchoolNo()) && null != schoolMaps.get(s.getSchoolNo())) {
                if (null != gradeMaps.get(s.getGradeId())) {
                    s.setGradeName(gradeMaps.get(s.getGradeId()).getName());
                }
                if (null != classMaps.get(s.getClassId())) {
                    s.setClassName(classMaps.get(s.getClassId()).getName());
                }
                s.setSchoolName(schoolMaps.get(s.getSchoolNo()).getName());
                s.setSchoolId(schoolMaps.get(s.getSchoolNo()).getId());
            }

            // 筛查次数
            s.setScreeningCount(countMaps.getOrDefault(s.getId(), 0));

            // TODO: 就诊次数
            s.setSeeDoctorCount(0);
            // TODO: 设置问卷数
            s.setQuestionnaireCount(0);
        });
        return pageStudents;
    }

    /**
     * 查询
     */
    public List<Student> getBy(StudentQuery query) {
        return baseMapper.getBy(query);
    }

    /**
     * 条件过滤
     *
     * @param gradeIdsStr     年级ID字符串
     * @param visionLabelsStr 视力标签字符串
     * @return {@link TwoTuple} <p>TwoTuple.getFirst-年级list, TwoTuple.getSecond-视力标签list</p>
     */
    public TwoTuple<List<Integer>, List<Integer>> conditionalFilter(String gradeIdsStr,
                                                                    String visionLabelsStr) {
        TwoTuple<List<Integer>, List<Integer>> result = new TwoTuple<>();

        // 年级条件
        if (StringUtils.isNotBlank(gradeIdsStr)) {
            result.setFirst(Arrays.stream(gradeIdsStr.split(","))
                    .map(Integer::valueOf).collect(Collectors.toList()));
        }

        // 视力标签条件
        if (StringUtils.isNotBlank(visionLabelsStr)) {
            result.setSecond(Arrays.stream(visionLabelsStr.split(","))
                    .map(Integer::valueOf).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * 获取学生筛查档案
     *
     * @param studentId 学生ID
     * @return StudentScreeningResultResponse
     */
    public StudentScreeningResultResponse getScreeningList(Integer studentId) {
        StudentScreeningResultResponse response = new StudentScreeningResultResponse();

        // 通过计划Ids查询学生的结果
        List<ScreeningResult> resultList = screeningResultService.getByStudentIds(studentId);
        response.setTotal(resultList.size());
        response.setItems(resultList);
        return response;
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<Student> getByPage(Page<?> page, StudentQuery query) {
        return baseMapper.getByPage(page, query);
    }

    /**
     * 通过id获取学生信息
     *
     * @param id 学生ID
     * @return StudentDTO
     */
    public StudentDTO getStudentById(Integer id) {
        StudentDTO student = baseMapper.getStudentById(id);

        if (StringUtils.isNotBlank(student.getSchoolNo())) {
            // 学校编号不为空，则拼接学校信息
            School school = schoolService.getBySchoolNo(student.getSchoolNo());
            student.setSchoolId(school.getId());
            student.setSchoolNo(school.getSchoolNo());
            student.setSchoolName(school.getName());
        }
        return student;
    }


    /**
     * 通过学校ID、班级ID、年级ID查找学生
     *
     * @param schoolId 学校Id
     * @return 学生列表
     */
    public List<Student> getBySchoolIdAndGradeIdAndClassId(Integer schoolId, Integer classId, Integer gradeId) {
        return baseMapper.getByOtherId(schoolId, classId, gradeId);
    }

    /**
     * 统计学生人数
     *
     * @return List<StudentCountVO>
     */
    public List<StudentCountVO> countStudentBySchoolNo() {
        return baseMapper.countStudentBySchoolNo();
    }


    /**
     * 检查学生身份证号码是否重复
     *
     * @param IdCard 身份证号码
     * @param id     学生ID
     * @return 是否重复
     */
    public Boolean checkIdCard(String IdCard, Integer id) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<Student>()
                .eq("id_card", IdCard);

        if (null != id) {
            queryWrapper.ne("id", id);
        }
        return baseMapper.selectList(queryWrapper).size() > 0;
    }

    /**
     * 根据身份证列表获取学生
     * @param idCardList
     * @return
     */
    public List<Student> getByIdCards(List<String> idCardList) {
        StudentQuery studentQuery = new StudentQuery();
        return Lists.partition(idCardList, 50).stream().map(list -> {
            studentQuery.setIdCardList(list);
            return baseMapper.getBy(studentQuery);
        }).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * 批量检查学生身份证号码是否重复
     *
     * @param IdCards 身份证号码
     * @return 是否重复
     */
    public Boolean checkIdCards(List<String> IdCards) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<Student>()
                .in("id_card", IdCards);
        return baseMapper.selectList(queryWrapper).size() > 0;
    }
}