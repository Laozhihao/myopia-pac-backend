package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.SchoolGradeItems;
import com.wupol.myopia.business.management.domain.dto.SchoolGradeResponseDto;
import com.wupol.myopia.business.management.domain.mapper.SchoolGradeMapper;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.model.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
public class SchoolGradeService extends BaseService<SchoolGradeMapper, SchoolGrade> {

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private StudentService studentService;

    @Transactional(rollbackFor = Exception.class)
    public Integer deletedGrade(Integer id) {

        // 判断年级是否班级使用
        List<SchoolClass> schoolClasses = schoolClassService.getSchoolClassByGradeId(id);
        if (!schoolClasses.isEmpty()) {
            throw new RuntimeException("当前年级被班级依赖，不能删除");
        }

        // 判断是否给学生使用
        List<Student> students = studentService.getStudentsByGradeId(id);
        if (!students.isEmpty()) {
            throw new RuntimeException("当前年级被学生依赖，不能删除");
        }
        SchoolGrade schoolGrade = new SchoolGrade();
        schoolGrade.setId(id);
        schoolGrade.setCreateUserId(1);
        schoolGrade.setStatus(Const.STATUS_IS_DELETED);
        return baseMapper.updateById(schoolGrade);
    }

    public Object getGradeList(Integer schoolId) {

        SchoolGradeResponseDto responseDto = new SchoolGradeResponseDto();
        List<SchoolGradeItems> schoolGradeItems = new ArrayList<>();

        QueryWrapper<SchoolGrade> schoolGradeWrapper = new QueryWrapper<>();
        schoolGradeWrapper.eq("school_id", schoolId).ne("status", Const.STATUS_IS_DELETED);

        // 获取年级
        List<SchoolGrade> schoolGrades = baseMapper.selectList(schoolGradeWrapper);
        if (schoolGrades.isEmpty()) {
            responseDto.setItems(schoolGradeItems);
            return responseDto;
        }
        Map<Integer, List<SchoolClass>> classMaps = schoolClassService.getSchoolClassByGradeIds(schoolGrades.stream().map(SchoolGrade::getId).collect(Collectors.toList()), schoolId).stream().collect(Collectors.groupingBy(SchoolClass::getGradeId));

        schoolGrades.forEach(g -> {
            SchoolGradeItems items = new SchoolGradeItems();
            items.setId(g.getId());
            items.setSchoolId(g.getSchoolId());
            items.setName(g.getName());
            items.setClasses(classMaps.get(g.getId()));
            schoolGradeItems.add(items);
        });
        responseDto.setItems(schoolGradeItems);
        return responseDto;
    }
}
