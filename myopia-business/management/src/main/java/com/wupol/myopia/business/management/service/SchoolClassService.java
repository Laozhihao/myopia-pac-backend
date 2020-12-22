package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.SchoolClassMapper;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.model.Student;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
public class SchoolClassService extends BaseService<SchoolClassMapper, SchoolClass> {

    @Resource
    private StudentService studentService;

    /**
     * 删除年级
     *
     * @param classId 年级id
     * @return 成功数
     */
    public Integer deletedClass(Integer classId) {

        // 判断是否给学生使用
        List<Student> students = studentService.getStudentsByClassId(classId);
        if (!students.isEmpty()) {
            throw new RuntimeException("当前年级被学生依赖，不能删除");
        }

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(classId);
        schoolClass.setCreateUserId(1);
        schoolClass.setStatus(Const.STATUS_IS_DELETED);
        return baseMapper.updateById(schoolClass);
    }

    /**
     * 通过年级获取班级
     *
     * @param gradeId 年级id
     * @return 班级列表
     */
    public List<SchoolClass> getSchoolClassByGradeId(Integer gradeId) {
        QueryWrapper<SchoolClass> schoolClassWrapper = new QueryWrapper<>();
        schoolClassWrapper.eq("grade_id", gradeId)
                .ne("status", Const.STATUS_IS_DELETED);
        return baseMapper.selectList(schoolClassWrapper);
    }

    /**
     * 批量通过年级获取班级
     *
     * @param gradeIds 年级idLists
     * @param schoolId 学校id
     * @return 班级列表
     */
    public List<SchoolClass> getSchoolClassByGradeIds(List<Integer> gradeIds, Integer schoolId) {
        QueryWrapper<SchoolClass> schoolClassWrapper = new QueryWrapper<>();
        schoolClassWrapper.in("grade_id", gradeIds)
                .eq("school_id", schoolId)
                .ne("status", Const.STATUS_IS_DELETED);
        return baseMapper.selectList(schoolClassWrapper);
    }


}
