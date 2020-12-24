package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.StudentMapper;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
public class StudentService extends BaseService<StudentMapper, Student> {

    @Resource
    private StudentMapper studentMapper;

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
        // 获取学校编码
        School school = schoolService.getById(student.getSchoolId());
        student.setStudentNo(generateOrgNo(school.getSchoolNo(), Const.GRADE_NO, student.getIdCard()));
        return baseMapper.insert(student);
    }

    /**
     * 更新学生
     *
     * @param student 学生实体类
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateStudent(Student student) {
        return baseMapper.updateById(student);
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
    public IPage<Student> getStudentLists(PageRequest pageRequest, StudentQuery studentQuery) {
        return studentMapper.getStudentListByCondition(pageRequest.toPage(), studentQuery.getSchoolId(),
                studentQuery.getSno(), studentQuery.getIdCard(), studentQuery.getName(),
                studentQuery.getParentPhone(), studentQuery.getGender(),
                studentQuery.getGradeId(), studentQuery.getClassId(), studentQuery.getLabels(),
                studentQuery.getStartScreeningTime(), studentQuery.getEndScreeningTime());
    }

    private String generateOrgNo(String schoolNo, Integer gradeNo, String idCard) {
        return StringUtils.join(schoolNo, gradeNo, StringUtils.right(idCard, 6));
    }
}
