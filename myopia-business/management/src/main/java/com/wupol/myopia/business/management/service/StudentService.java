package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.mapper.StudentMapper;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
@Log4j2
public class StudentService extends BaseService<StudentMapper, Student> {

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private SchoolClassService schoolClassService;

    /**
     * 通过学校id查找学生
     *
     * @param schoolId 学校Id
     * @return 学生列表
     */
    public List<Student> getStudentsBySchoolId(Integer schoolId) {
        QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
        equalsQueryAppend(studentQueryWrapper, "school_id", schoolId);
        notEqualsQueryAppend(studentQueryWrapper, "status", Const.STATUS_IS_DELETED);
        return baseMapper.selectList(studentQueryWrapper);
    }

    /**
     * 通过年级id查找学生
     *
     * @param gradeId 年级Id
     * @return 学生列表
     */
    public List<Student> getStudentsByGradeId(Integer gradeId) {
        QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
        equalsQueryAppend(studentQueryWrapper, "grade_id", gradeId);
        notEqualsQueryAppend(studentQueryWrapper, "status", Const.STATUS_IS_DELETED);
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
        notEqualsQueryAppend(studentQueryWrapper, "status", Const.STATUS_IS_DELETED);
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

        // 获取学校编码
        School school = schoolService.getById(student.getSchoolId());
        // 获取年级编码
        SchoolGrade grade = schoolGradeService.getById(student.getGradeId());

        RLock rLock = redissonClient.getLock(Const.LOCK_STUDENT_REDIS + idCard);
        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {
                student.setStudentNo(generateOrgNo(school.getSchoolNo(), grade.getGradeCode(), idCard));
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
        baseMapper.updateById(student);
        Student resultStudent = baseMapper.selectById(student.getId());
        StudentDTO studentDTO = new StudentDTO();
        BeanUtils.copyProperties(resultStudent, studentDTO);
        // 查询年级和班级
        SchoolGrade schoolGrade = schoolGradeService.getById(resultStudent.getGradeId());
        SchoolClass schoolClass = schoolClassService.getById(resultStudent.getClassId());
        return studentDTO.setGradeName(schoolGrade.getName()).setClassName(schoolClass.getName());
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
        student.setStatus(Const.STATUS_IS_DELETED);
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
        List<Integer> gradeIds = new ArrayList<>();
        if (StringUtils.isNotBlank(studentQuery.getGradeIds())) {
            gradeIds = Arrays.stream(studentQuery.getGradeIds().split(",")).map(Integer::valueOf).collect(Collectors.toList());
        }
        IPage<StudentDTO> pageStudents = studentMapper.getStudentListByCondition(pageRequest.toPage(), studentQuery.getSchoolId(),
                studentQuery.getSno(), studentQuery.getIdCard(), studentQuery.getName(),
                studentQuery.getParentPhone(), studentQuery.getGender(),
                gradeIds, studentQuery.getLabels(), studentQuery.getStartScreeningTime(),
                studentQuery.getEndScreeningTime());
        List<StudentDTO> students = pageStudents.getRecords();
        if (CollectionUtils.isEmpty(students)) {
            return pageStudents;
        }
        Map<Integer, SchoolGrade> gradeMaps = schoolGradeService
                .getByIds(students
                        .stream()
                        .map(Student::getGradeId)
                        .collect(Collectors.toList()))
                .stream()
                .collect(Collectors
                        .toMap(SchoolGrade::getId, Function.identity()));
        Map<Integer, SchoolClass> classMaps = schoolClassService
                .getByIds(students
                        .stream()
                        .map(Student::getClassId)
                        .collect(Collectors.toList()))
                .stream()
                .collect(Collectors
                        .toMap(SchoolClass::getId, Function.identity()));
        students.forEach(s -> {
            s.setGradeName(gradeMaps.get(s.getGradeId()).getName());
            s.setClassName(classMaps.get(s.getClassId()).getName());
        });
        return pageStudents;
    }

    private String generateOrgNo(String schoolNo, String gradeNo, String idCard) {
        return StringUtils.join(schoolNo, gradeNo, StringUtils.right(idCard, 6));
    }

    /**
     * 获取导出数据
     */
    public List<Student> getExportData(StudentQuery query) {
        return baseMapper.getExportData(query);
    }


}
