package com.wupol.myopia.business.management.service;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanSchoolStudentMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 同学生ID获取计划
     *
     * @param studentId 学生ID
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getByStudentId(Integer studentId) {
    /**
     * 根据学生id获取筛查计划学校学生
     * @param studentId
     * @return
     */
    List<ScreeningPlanSchoolStudent> getByStudentId(Integer studentId) {
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
}
