package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.StudentListRequest;
import com.wupol.myopia.business.management.domain.mapper.StudentMapper;
import com.wupol.myopia.business.management.domain.model.Student;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;

import static com.wupol.myopia.base.util.DateFormatUtil.FORMAT_DETAIL_TIME;

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
        student.setStudentNo(generateStudentNo());
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
     * @param request 请求体
     * @return Page<Student> {@link Page}
     * @throws ParseException 转换异常
     */
    public Page<Student> getStudentLists(StudentListRequest request) throws ParseException {
        Page<Student> page = new Page<>(request.getCurrent(), request.getSize());
        QueryWrapper<Student> studentWrapper = new QueryWrapper<>();

        equalsQueryAppend(studentWrapper, "school_id", request.getSchoolId());
        notEqualsQueryAppend(studentWrapper, "status", Const.STATUS_IS_DELETED);

        if (null != request.getSno()) {
            likeQueryAppend(studentWrapper, "sno", request.getSno());
        }
        if (StringUtils.isNotBlank(request.getIdCard())) {
            likeQueryAppend(studentWrapper, "id_card", request.getIdCard());
        }
        if (StringUtils.isNotBlank(request.getName())) {
            likeQueryAppend(studentWrapper, "name", request.getName());
        }
        if (StringUtils.isNotBlank(request.getParentPhone())) {
            likeQueryAppend(studentWrapper, "parent_phone", request.getParentPhone());
        }
        if (null != request.getGender()) {
            equalsQueryAppend(studentWrapper, "gender", request.getGender());
        }
        if (null != request.getGradeId()) {
            equalsQueryAppend(studentWrapper, "grade_id", request.getGradeId());
        }
        if (null != request.getClassId()) {
            equalsQueryAppend(studentWrapper, "class_id", request.getClassId());
        }
        if (StringUtils.isNotBlank(request.getLabels())) {
            likeQueryAppend(studentWrapper, "labels", request.getLabels());
        }
        if (null != request.getStartScreeningTime() && null != request.getEndScreeningTime()) {
            betweenQueryAppend(studentWrapper, "last_screening_time",
                    DateFormatUtil.parseDate(request.getStartScreeningTime(), FORMAT_DETAIL_TIME),
                    DateFormatUtil.parseDate(request.getEndScreeningTime(), FORMAT_DETAIL_TIME));
        }
        return baseMapper.selectPage(page, studentWrapper);
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
