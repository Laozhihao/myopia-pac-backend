package com.wupol.myopia.business.screening.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.SchoolClassMapper;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import com.wupol.myopia.business.management.service.SchoolClassService;
import com.wupol.myopia.business.management.service.SchoolGradeService;
import com.wupol.myopia.business.management.service.SchoolService;
import com.wupol.myopia.business.management.service.StudentService;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.connection.decoder.ListDrainToDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 筛查端App接口
 *
 * @Author Chikong
 * @Date 2021-01-21
 */
@Service
public class ScreeningAppService {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private StudentService studentService;

    /**
     * 模糊查询所有学校名称
     * @param nameLike 模糊查询
     * @param deptId    机构id
     * @return
     */
    public List<String> getSchoolNameBySchoolNameLike(String nameLike, Integer deptId) {
        SchoolQuery query = new SchoolQuery().setNameLike(nameLike);
        query.setGovDeptId(deptId);
        return schoolService.getBy(query).stream().map(School::getName).collect(Collectors.toList());
    }

    /**
     * 查询学校的年级名称
     * @param schoolName 学校名
     * @param deptId    机构id
     * @return
     */
    public List<String> getGradeNameBySchoolName(String schoolName, Integer deptId) {
        return schoolGradeService.getBySchoolName(schoolName, deptId).stream().map(SchoolGrade::getName).collect(Collectors.toList());
    }


    /**
     * 获取学校年级的班级名称
     * @param schoolName    学校名称
     * @param gradeName     年级名称
     * @param deptId        部门id
     * @return
     */
    public List<String> getClassNameBySchoolNameAndGradeName(String schoolName, String gradeName, Integer deptId) {
        return schoolClassService.getBySchoolNameAndGradeName(schoolName, gradeName, deptId).stream()
                .map(SchoolClass::getName).collect(Collectors.toList());
    }

    /**
     * 获取学校年级班级对应的学生名称
     * @param schoolId      学校id, 仅复测时有
     * @param schoolName    学校名称
     * @param gradeName     年级名称
     * @param clazzName     班级名称
     * @param deptId        部门id
     * @param isReview      是否复测
     * @return
     */
    public List<Student> getStudentNameBySchoolNameAndGradeNameAndClassName(PageRequest pageRequest,
                                                                            Integer schoolId,
                                                                            String schoolName,
                                                                            String gradeName,
                                                                            String clazzName,
                                                                            Integer deptId,
                                                                            Boolean isReview) {
        //TODO 待增加复测的逻辑
        List<SchoolClass> classList = schoolClassService.getBySchoolNameAndGradeName(schoolName, gradeName, deptId);
        for (SchoolClass item : classList) {
            if (item.getName().equals(clazzName)) { // 匹配对应的班级
                return studentService.getStudentsByClassId(item.getId());
            }
        }
        return Collections.emptyList();

    }

    /**
     * 获取筛查就机构对应的学校
     * @param deptId        部门id
     * @return
     */
    public List<School> getSchoolByDeptId(Integer deptId) {
        SchoolQuery query = new SchoolQuery();
        //TODO 待修改成机构
        query.setGovDeptId(deptId);
        return schoolService.getBy(query);
    }
}