package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.StudentMapper;
import com.wupol.myopia.business.management.domain.model.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
public class StudentService extends BaseService<StudentMapper, Student> {

    /**
     * 通过年级id查找学生
     *
     * @param gradeId 年级Id
     * @return 学生列表
     */
    public List<Student> getStudentsByGradeId(Integer gradeId) {
        QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
        studentQueryWrapper.eq("grade_id", gradeId).ne("status", Const.STATUS_IS_DELETED);
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
        studentQueryWrapper.eq("class_id", classId).ne("status", Const.STATUS_IS_DELETED);
        return baseMapper.selectList(studentQueryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer saveStudent(Student student) {
        student.setStudentNo(generateStudentNo());
        return baseMapper.insert(student);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer updateStudent(Student student) {
        return baseMapper.updateById(student);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer deletedStudent(Integer id) {
        Student student = new Student();
        student.setId(id);
        student.setStatus(Const.STATUS_IS_DELETED);
        return baseMapper.updateById(student);
    }

    /**
     * 生成编号
     *
     * @return Long
     */
    private Long generateStudentNo() {
        return 123L;
    }

}
