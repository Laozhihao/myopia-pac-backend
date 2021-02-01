package com.wupol.myopia.business.management.service;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanSchoolStudentMapper;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.vo.SchoolGradeVo;
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanSchoolVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScreeningPlanSchoolStudentService extends BaseService<ScreeningPlanSchoolStudentMapper, ScreeningPlanSchoolStudent> {

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
     * @param screeningPlanId
     * @param excludeSchoolIds
     */
    public void deleteByPlanIdAndExcludeSchoolIds(Integer screeningPlanId, List<Integer> excludeSchoolIds) {
        Assert.notNull(screeningPlanId);
        QueryWrapper<ScreeningPlanSchoolStudent> query = new QueryWrapper<ScreeningPlanSchoolStudent>().eq("screening_plan_id", screeningPlanId);
        if (!CollectionUtils.isEmpty(excludeSchoolIds)) {
            query.notIn("school_id", excludeSchoolIds);
        }
        baseMapper.delete(query);
    }

    /**
     * 根据计划ID获取所有筛查学生
     * @param screeningPlanId
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByScreeningPlanId(Integer screeningPlanId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningPlanSchoolStudent>().eq("screening_plan_id", screeningPlanId));
    }

    /**
     * 根据计划ID获取学校ID的学生数Map
     * @param screeningPlanId
     * @return
     */
    public Map<Integer, Long> getSchoolStudentCountByScreeningPlanId(Integer screeningPlanId) {
        return getByScreeningPlanId(screeningPlanId).stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId, Collectors.counting()));
    }

    /**
     * 获取计划中的学校年级情况
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
}
