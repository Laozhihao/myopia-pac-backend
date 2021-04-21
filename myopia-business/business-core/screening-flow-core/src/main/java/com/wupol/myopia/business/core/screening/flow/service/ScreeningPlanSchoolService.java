package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.CollectionUtils;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.constant.ScreeningConstant;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanSchoolDTO;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningPlanSchoolMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanSchoolMapper;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.management.domain.query.ScreeningPlanQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanSchoolDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScreeningPlanSchoolService extends BaseService<ScreeningPlanSchoolMapper, ScreeningPlanSchool> {

    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    /**
     * 根据学校ID获取筛查计划的学校
     *
     * @param schoolId 学校ID
     * @return List<ScreeningPlanSchool>
     */
    public List<ScreeningPlanSchool> getBySchoolId(Integer schoolId) {
        return baseMapper.getBySchoolId(schoolId);
    }

    /**
     * 查询计划的学校
     *
     * @param screeningPlanId 计划ID
     * @param schoolId 学校ID
     * @return ScreeningPlanSchool
     */
    public ScreeningPlanSchool getOneByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        return baseMapper.getOneByPlanIdAndSchoolId(screeningPlanId,schoolId);
    }

    /**
     * 批量更新或新增筛查计划的学校信息
     *
     * @param screeningPlanId 计划ID
     * @param screeningOrgId 机构ID
     * @param screeningSchools 筛查计划关联的学校
     */
    public void saveOrUpdateBatchWithDeleteExcludeSchoolsByPlanId(Integer screeningPlanId, Integer screeningOrgId, List<ScreeningPlanSchool> screeningSchools) {
        // 删除掉已有的不存在的学校信息
        List<Integer> excludeSchoolIds = CollectionUtils.isEmpty(screeningSchools) ? Collections.EMPTY_LIST : screeningSchools.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList());
        deleteByPlanIdAndExcludeSchoolIds(screeningPlanId, excludeSchoolIds);
        if (!CollectionUtils.isEmpty(screeningSchools)) {
            saveOrUpdateBatchByPlanId(screeningPlanId, screeningOrgId, screeningSchools);
        }
    }

    /**
     * 批量更新或新增筛查计划的学校信息
     *
     * @param screeningPlanId 筛查计划
     * @param screeningSchools 筛查计划关联的学校
     */
    public void saveOrUpdateBatchByPlanId(Integer screeningPlanId, Integer screeningOrgId, List<ScreeningPlanSchool> screeningSchools) {
        // 1. 查出剩余的
        Map<Integer, Integer> schoolIdMap = getSchoolListsByPlanId(screeningPlanId).stream().collect(Collectors.toMap(ScreeningPlanSchool::getSchoolId, ScreeningPlanSchool::getId));
        // 2. 更新id，并批量新增或修改
        screeningSchools.forEach(planSchool -> planSchool.setScreeningPlanId(screeningPlanId).setScreeningOrgId(screeningOrgId).setId(schoolIdMap.getOrDefault(planSchool.getSchoolId(), null)));
        saveOrUpdateBatch(screeningSchools);
    }

    /**
     * 通过筛查计划ID获取所有关联的学校信息
     *
     * @param screeningPlanId 筛查计划ID
     * @return List<ScreeningPlanSchool>
     */
    public List<ScreeningPlanSchool> getSchoolListsByPlanId(Integer screeningPlanId) {
        return baseMapper.getByPlanId(screeningPlanId);
    }

    /**
     * 通过筛查计划ID获取所有关联的学校vo信息
     *
     * @param screeningPlanId 筛查计划ID
     * @return List<ScreeningPlanSchoolDTO>
     */
    public List<ScreeningPlanSchoolDTO> getSchoolVoListsByPlanId(Integer screeningPlanId) {
        List<ScreeningPlanSchoolDTO> screeningPlanSchools = baseMapper.selectVoListByPlanId(screeningPlanId);
        Map<Integer, Long> schoolIdStudentCountMap = screeningPlanSchoolStudentService.getSchoolStudentCountByScreeningPlanId(screeningPlanId);
        screeningPlanSchools.forEach(vo -> vo.setStudentCount(schoolIdStudentCountMap.getOrDefault(vo.getSchoolId(), (long) 0).intValue()));
        return screeningPlanSchools;
    }

    /**
     * 删除筛查计划中，除了指定学校ID的其它学校信息
     *
     * @param screeningPlanId 筛查计划ID
     * @param excludeSchoolIds 排除的学校ID
     */
    public void deleteByPlanIdAndExcludeSchoolIds(Integer screeningPlanId, List<Integer> excludeSchoolIds) {
        Assert.notNull(screeningPlanId);
        baseMapper.deleteByPlanIdAndExcludeSchoolIds(screeningPlanId, excludeSchoolIds);
        screeningPlanSchoolStudentService.deleteByPlanIdAndExcludeSchoolIds(screeningPlanId, excludeSchoolIds);
    }

    /**
     * 查询已有计划的学校 （层级ID列表与筛查机构ID必须有一个不为空）
     *
     * @param districtIds             层级ID列表
     * @param excludedScreeningPlanId 排除的计划ID
     * @param screeningOrgId          筛查机构ID
     * @param startTime               查询计划的起始时间
     * @param endTime                 查询计划的结束时间
     * @return 学校ID
     */
    public List<Integer> getHavePlanSchoolIds(List<Integer> districtIds, Integer excludedScreeningPlanId, Integer screeningOrgId, LocalDate startTime, LocalDate endTime) {
        if (CollectionUtils.isEmpty(districtIds) && Objects.isNull(screeningOrgId)) {
            return Collections.emptyList();
        }
        ScreeningPlanQuery planQuery = new ScreeningPlanQuery();
        planQuery.setDistrictIds(districtIds).setExcludedScreeningPlanId(excludedScreeningPlanId).setStartCreateTime(startTime).setEndCreateTime(endTime).setScreeningOrgId(screeningOrgId);
        return baseMapper.selectHasPlanInPeriod(planQuery).stream().map(ScreeningPlanSchool::getSchoolId).distinct().collect(Collectors.toList());
    }

    /**
     * 获取该筛查机构目前的筛查学校
     *
     * @param schoolName 学校名称
     * @param deptId 机构ID
     * @return 学校列表
     */
    public List<School> getSchoolByOrgId(String schoolName, Integer deptId) {
        if (deptId == null) {
            throw new ManagementUncheckedException("deptId 不能为空");
        }

        List<Long> schoolIds = screeningPlanService.getScreeningSchoolIdByScreeningOrgId(deptId);
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        return schoolService.getSchoolByIdsAndName(schoolIds,schoolName);
    }

    /**
     * 更新学校名称
     *
     * @param schoolId   学校ID
     * @param schoolName 学校名称
     */
    public void updateSchoolNameBySchoolId(Integer schoolId, String schoolName) {
        baseMapper.updateSchoolNameBySchoolId(schoolId, schoolName);
    }

    /**
     * 通过学校id获取筛查计划关联的学校
     *
     * @param schoolIds 学校Ids
     * @return List<ScreeningPlanSchool>
     */
    public List<ScreeningPlanSchool> getBySchoolIds(List<Integer> schoolIds) {
        return baseMapper.getBySchoolIds(schoolIds);
    }

    /**
     * 获取筛查机构正在筛查的学校ID
     *
     * @param screeningOrgId
     * @return
     */
    public List<ScreeningPlanSchool> getScreeningSchoolsByScreeningOrgId(Integer screeningOrgId) {
        return baseMapper.getScreeningSchoolsByOrgId(screeningOrgId, ScreeningConstant.SCREENING_RELEASE_STATUS, new Date());
    }
}
