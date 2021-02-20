package com.wupol.myopia.business.management.service;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sun.org.apache.bcel.internal.generic.INEG;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanSchoolMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.management.domain.vo.SchoolScreeningCountVO;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.management.domain.query.ScreeningPlanQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanSchoolVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScreeningPlanSchoolService extends BaseService<ScreeningPlanSchoolMapper, ScreeningPlanSchool> {

    /**
     * 通过学校ID获取计划
     *
     * @param schoolId 学校ID
     * @return List<ScreeningPlanSchool>
     */
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    /**
     * 根据学校ID获取筛查计划的学校
     * @param schoolId
     * @return
     */
    public List<ScreeningPlanSchool> getBySchoolId(Integer schoolId) {
        return baseMapper
                .selectList(new QueryWrapper<ScreeningPlanSchool>()
                        .eq("school_id", schoolId));
    }

    /**
     * 学校筛查统计
     * @return List<SchoolScreeningCountVO>
     */
    public List<SchoolScreeningCountVO> countScreeningTime() {
        return baseMapper.countScreeningTime();
    }

    /**
     * 查询计划的学校
     * @param screeningPlanId
     * @param schoolId
     * @return
     */
    public ScreeningPlanSchool getOne(Integer screeningPlanId, Integer schoolId) {
        return baseMapper.selectOne(new QueryWrapper<ScreeningPlanSchool>().eq("screening_plan_id", screeningPlanId).eq("school_id", schoolId));
    }

    /**
     * 批量更新或新增筛查计划的学校信息
     * @param screeningPlanId
     * @param screeningSchools
     */
    public void saveOrUpdateBatchWithDeleteExcludeSchoolsByPlanId(Integer screeningPlanId, List<ScreeningPlanSchool> screeningSchools) {
        // 删除掉已有的不存在的学校信息
        List<Integer> excludeSchoolIds = CollectionUtils.isEmpty(screeningSchools) ? Collections.EMPTY_LIST : screeningSchools.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList());
        deleteByPlanIdAndExcludeSchoolIds(screeningPlanId, excludeSchoolIds);
        if (!CollectionUtils.isEmpty(screeningSchools)) {
            saveOrUpdateBatchByPlanId(screeningPlanId, screeningSchools);
        }
    }

    /**
     * 批量更新或新增筛查计划的学校信息
     * @param screeningPlanId
     * @param screeningSchools
     */
    public void saveOrUpdateBatchByPlanId(Integer screeningPlanId, List<ScreeningPlanSchool> screeningSchools) {
        // 1. 查出剩余的
        Map<Integer, Integer> schoolIdMap = getSchoolListsByPlanId(screeningPlanId).stream().collect(Collectors.toMap(ScreeningPlanSchool::getSchoolId, ScreeningPlanSchool::getId));
        // 2. 更新id，并批量新增或修改
        screeningSchools.forEach(planSchool -> planSchool.setScreeningPlanId(screeningPlanId).setId(schoolIdMap.getOrDefault(planSchool.getSchoolId(), null)));
        saveOrUpdateBatch(screeningSchools);
    }

    /**
     * 通过筛查计划ID获取所有关联的学校信息
     *
     * @param screeningPlanId
     * @return
     */
    public List<ScreeningPlanSchool> getSchoolListsByPlanId(Integer screeningPlanId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningPlanSchool>().eq("screening_plan_id", screeningPlanId));
    }

    /**
     * 通过筛查计划ID获取所有关联的学校vo信息
     *
     * @param screeningPlanId
     * @return
     */
    public List<ScreeningPlanSchoolVo> getSchoolVoListsByPlanId(Integer screeningPlanId) {
        List<ScreeningPlanSchoolVo> screeningPlanSchools = baseMapper.selectVoListByPlanId(screeningPlanId);
        Map<Integer, Long> schoolIdStudentCountMap = screeningPlanSchoolStudentService.getSchoolStudentCountByScreeningPlanId(screeningPlanId);
        screeningPlanSchools.forEach(vo -> vo.setStudentCount(schoolIdStudentCountMap.getOrDefault(vo.getSchoolId(), (long) 0).intValue()));
        return screeningPlanSchools;
    }

    /**
     * 删除筛查计划中，除了指定学校ID的其它学校信息
     * @param screeningPlanId
     * @param excludeSchoolIds
     */
    public void deleteByPlanIdAndExcludeSchoolIds(Integer screeningPlanId, List<Integer> excludeSchoolIds) {
        Assert.notNull(screeningPlanId);
        QueryWrapper<ScreeningPlanSchool> query = new QueryWrapper<ScreeningPlanSchool>().eq("screening_plan_id", screeningPlanId);
        if (!CollectionUtils.isEmpty(excludeSchoolIds)) {
            query.notIn("school_id", excludeSchoolIds);
        }
        baseMapper.delete(query);
        screeningPlanSchoolStudentService.deleteByPlanIdAndExcludeSchoolIds(screeningPlanId, excludeSchoolIds);
    }

    /**
     * 查询已有计划的学校 （层级ID列表与筛查机构ID必须有一个不为空）
     * @param districtIds 层级ID列表
     * @param excludedScreeningPlanId 排除的计划ID
     * @param screeningOrgId 筛查机构ID
     * @param startTime 查询计划的起始时间
     * @param endTime 查询计划的结束时间
     * @return
     */
    public List<Integer> getHavePlanSchoolIds(List<Integer> districtIds, Integer excludedScreeningPlanId, Integer screeningOrgId, LocalDate startTime, LocalDate endTime) {
        if (CollectionUtils.isEmpty(districtIds) && Objects.isNull(screeningOrgId)) {
            return Collections.emptyList();
        }
        ScreeningPlanQuery planQuery = new ScreeningPlanQuery();
        planQuery.setDistrictIds(districtIds).setExcludedScreeningPlanId(excludedScreeningPlanId).setStartCreateTime(startTime).setEndCreateTime(endTime).setScreeningOrgId(screeningOrgId);
        return baseMapper.selectHasPlanInPeriod(planQuery).stream().map(ScreeningPlanSchool::getSchoolId).distinct().collect(Collectors.toList());
    }
}
