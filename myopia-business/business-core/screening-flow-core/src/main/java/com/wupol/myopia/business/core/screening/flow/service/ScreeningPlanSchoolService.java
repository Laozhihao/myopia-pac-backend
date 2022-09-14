package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.ScreeningConstant;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningListResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanListDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanSchoolDTO;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningPlanSchoolMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
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
     * @param schoolId        学校ID
     * @return ScreeningPlanSchool
     */
    public ScreeningPlanSchool getOneByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        return baseMapper.getOneByPlanIdAndSchoolId(screeningPlanId, schoolId);
    }

    /**
     * 批量更新或新增筛查计划的学校信息
     *
     * @param screeningPlanId  计划ID
     * @param screeningOrgId   机构ID
     * @param screeningSchools 筛查计划关联的学校
     */
    public void saveOrUpdateBatchWithDeleteExcludeSchoolsByPlanId(Integer screeningPlanId, Integer screeningOrgId, List<ScreeningPlanSchool> screeningSchools) {
        // 删除掉已有的不存在的学校信息
        List<Integer> excludeSchoolIds = CollectionUtils.isEmpty(screeningSchools) ? Collections.emptyList() : screeningSchools.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList());
        deleteByPlanIdAndExcludeSchoolIds(screeningPlanId, excludeSchoolIds);
        if (!CollectionUtils.isEmpty(screeningSchools)) {
            saveOrUpdateBatchByPlanId(screeningPlanId, screeningOrgId, screeningSchools);
        }
    }

    /**
     * 批量更新或新增筛查计划的学校信息
     *
     * @param screeningPlanId  筛查计划
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
     * 根据计划id和学校名获得计划
     * TODO: 此方法根据筛查计划ID和学校名称模糊查询
     *
     * @param screeningPlanId
     * @param schoolName
     * @return
     */
    public List<ScreeningPlanSchoolDTO> getScreeningPlanSchools(Integer screeningPlanId, String schoolName){
        return baseMapper.selectVoListByPlanId(screeningPlanId, schoolName);
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
     * 删除筛查计划中学校信息
     *
     * @param screeningPlanId 筛查计划ID
     */
    public void deleteByPlanId(Integer screeningPlanId){
        baseMapper.delete(Wrappers.lambdaQuery(ScreeningPlanSchool.class)
                .eq(ScreeningPlanSchool::getScreeningPlanId,screeningPlanId));
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
    public List<Integer> getHavePlanSchoolIds(List<Integer> districtIds, Integer excludedScreeningPlanId, Integer screeningOrgId, LocalDate startTime, LocalDate endTime,Integer screeningType) {
        if (CollectionUtils.isEmpty(districtIds) && Objects.isNull(screeningOrgId)) {
            return Collections.emptyList();
        }
        ScreeningPlanQueryDTO planQuery = new ScreeningPlanQueryDTO();
        planQuery.setDistrictIds(districtIds).setExcludedScreeningPlanId(excludedScreeningPlanId).setStartCreateTime(startTime).setEndCreateTime(endTime).setScreeningOrgId(screeningOrgId).setScreeningType(screeningType);
        return baseMapper.selectHasPlanInPeriod(planQuery).stream().map(ScreeningPlanSchool::getSchoolId).distinct().collect(Collectors.toList());
    }

    /**
     * 根据筛查计划获取筛查学校ID集
     *
     * @param screeningPlanIds  筛查计划ID集
     * @return java.util.Set<java.lang.Integer>
     **/
    public Set<Integer> getSchoolIdsByPlanIds(List<Integer> screeningPlanIds) {
        return getByPlanIds(screeningPlanIds).stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toSet());
    }

    /**
     * 根据筛查计划获取筛查学校
     *
     * @param screeningPlanIds  筛查计划ID集
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool>
     **/
    public List<ScreeningPlanSchool> getByPlanIds(List<Integer> screeningPlanIds) {
        if (CollectionUtils.isEmpty(screeningPlanIds)) {
            return Collections.emptyList();
        }
        return baseMapper.getByPlanIds(screeningPlanIds);
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
     * 获取筛查机构正在筛查的学校ID（属于已发布计划的）
     *
     * @param screeningOrgId
     * @return
     */
    public List<ScreeningPlanSchool> getReleasePlanScreeningSchoolsByScreeningOrgId(Integer screeningOrgId) {
        return baseMapper.getScreeningSchoolsByOrgId(screeningOrgId, ScreeningConstant.SCREENING_RELEASE_STATUS, new Date());
    }

    /**
     * 获取除当前学校外的学校
     *
     * @param planId    计划Id
     * @param schoolIds 学校Ids
     * @return 学校Ids
     */
    public List<Integer> getByPlanIdNotInSchoolIds(Integer planId, List<Integer> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return new ArrayList<>();
        }
        List<Integer> otherSchoolIds = baseMapper.getByPlanIdNotInSchoolIds(planId, schoolIds);
        if (CollectionUtils.isEmpty(otherSchoolIds)) {
            return new ArrayList<>();
        }
        return otherSchoolIds;
    }

    /**
     * 获取最大的筛查编号
     *
     * @return java.lang.Long
     **/
    public Long getCurrentMaxScreeningCode() {
        return baseMapper.getCurrentMaxScreeningCode();
    }

    /**
     * 通过学校ID获取计划
     *
     * @param pageRequest 分页请求
     * @param schoolId    学校Id
     * @return IPage<ScreeningListResponseDTO>
     */
    public IPage<ScreeningListResponseDTO> getReleasePlanSchoolPageBySchoolId(PageRequest pageRequest, Integer schoolId) {
        return baseMapper.getReleasePlanSchoolPageBySchoolId(pageRequest.toPage(), schoolId);
    }

    /**
     * 通过筛查条件查询列表
     * @param screeningPlanListDTO 筛查条件对象
     */
    public IPage<ScreeningListResponseDTO> listByCondition(ScreeningPlanListDTO screeningPlanListDTO) {
        return baseMapper.listByCondition(screeningPlanListDTO.toPage(), screeningPlanListDTO);
    }


    /**
     * 通过学校标识并指定筛查类型获取信息
     * @param schoolId
     * @param screeningType
     * @return
     */
    public ScreeningPlanSchool getLastBySchoolIdAndScreeningType(Integer schoolId, Integer screeningType) {
        return baseMapper.getLastBySchoolIdAndScreeningType(schoolId, screeningType);
    }

    public List<ScreeningPlanSchool> getPlansByPlanIdsAndSchoolNameLike(List<ScreeningPlan> plans,String schoolName){
        return baseMapper.selectList(new LambdaQueryWrapper<ScreeningPlanSchool>()
                .in(!CollectionUtils.isEmpty(plans), ScreeningPlanSchool::getScreeningPlanId, plans.stream().map(ScreeningPlan::getId).collect(Collectors.toList()))
                .like(Objects.nonNull(schoolName), ScreeningPlanSchool::getSchoolName, schoolName)
                .orderByDesc(ScreeningPlanSchool::getCreateTime));
    }

    /**
     * 根据学校ID获取筛查计划
     *
     * @param schoolId 学校ID
     * @return ScreeningPlanSchool
     */
    public ScreeningPlanSchool getOneBySchoolId(Integer schoolId) {
        return baseMapper.getBySchoolId(schoolId).stream().findFirst().orElse(null);
    }
}